package vttp.batchb.csf.project.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;

import vttp.batchb.csf.project.models.User;
import vttp.batchb.csf.project.repository.UserInterestTagRepository;
import vttp.batchb.csf.project.repository.UserRepository;
import vttp.batchb.csf.project.service.S3Service;
import vttp.batchb.csf.project.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    

    private final S3Service s3Service;
    private final UserRepository userRepo;
    private final UserInterestTagRepository tagRepo;
    private final UserService userService; 

    @Value("${stripe.refreshUrl}")
        private String refreshUrl;
    @Value("${stripe.returnUrl}")
        private String returnUrl;

    public UserController(S3Service s3Service, UserRepository userRepo,UserInterestTagRepository tagRepo,UserService userService) {
        this.s3Service = s3Service;
        this.userRepo = userRepo;
        this.tagRepo= tagRepo;
        this.userService = userService;
    }
    String bucketName = "csfprojectlx";
    String endPoint = "sgp1.digitaloceanspaces.com";
    @PostMapping("/{userId}/profile-picture")
    public ResponseEntity<?> updateProfilePicture(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file
            ) {
                try {

                    String oldKey = userRepo.getOldProfilePicKey(userId);

                    String newUrl = s3Service.uploadProfilePicture(file, userId);
                    String prefix = "https://" + bucketName + "." + endPoint + "/";

                    boolean updated = userRepo.updateProfilePicture(userId, newUrl);
                    if (!updated) {
                        return ResponseEntity.badRequest().body("Failed to update user profile picture in DB");
                    }

                    if (oldKey != null && !oldKey.isBlank()) {
                        String newKey = newUrl.substring(prefix.length()); 
                        if (!oldKey.equals(newKey)) {
                            s3Service.deleteFile(oldKey);
                        }
                    }

                    User user = userService.getUserById(userId);
                    if (user == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found after update");
                    }
                    
                    Map<String, Object> result = Map.of(
                        "userId", user.getUserId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "profilePicUrl", user.getProfilePictureUrl()
                    );
                    return ResponseEntity.ok(result);        
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.internalServerError().body("Error uploading file");
                }
            }
        
        @PutMapping("/{userId}/tags")
        public ResponseEntity<?> setTagsForUser(
            @PathVariable String userId,
            @RequestBody List<String> newTags
        ) {
            tagRepo.removeAllTagsForUser(userId);
            tagRepo.insertTags(userId, newTags);
            Map<String, String> result = Map.of("message", "Updated tags", "userId", userId);
            return ResponseEntity.ok(result);
            }
    
        @GetMapping("/{userId}/tags")
        public ResponseEntity<List<String>> getTagsForUser(@PathVariable String userId) {
            List<String> userTags = tagRepo.findTagsForUser(userId);
            return ResponseEntity.ok(userTags);
        }

        @GetMapping("/check-username")
        public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
            boolean exists = userRepo.existsByUsername(username);
            return ResponseEntity.ok(exists);
        }
        @PatchMapping("/{userId}/meetingLocation")
        public ResponseEntity<?> updateMeetingLocation(
            @PathVariable String userId,
            @RequestBody Map<String, String> body
        ) {
            String location = body.get("location");
        
            boolean success = userRepo.updatePreferredLocation(userId, location);
            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body(Map.of("message", "Failed to update location"));
            }
        
            User updatedUser = userService.getUserById(userId);
            if (updatedUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(Map.of("message", "User not found after update"));
            }
        
            return ResponseEntity.ok(Map.of(
                "userId", updatedUser.getUserId(),
                "username", updatedUser.getUsername(),
                "profilePicUrl", updatedUser.getProfilePictureUrl(),
                "preferredMeetingLocation", updatedUser.getPreferredMeetingLocation()
            ));
        }

        @PostMapping("/{userId}/create-stripe-account")
        public ResponseEntity<?> createStripeConnectAccount(@PathVariable String userId) {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }
    
            if (user.getStripe_account_id() != null && !user.getStripe_account_id().isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "User already has a Stripe account"));
            }
    
            try {
                AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("SG")
                    .setEmail(user.getEmail())
                    .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                            .setCardPayments(
                                AccountCreateParams.Capabilities.CardPayments.builder()
                                    .setRequested(true)
                                    .build()
                            )
                            .setTransfers(
                                AccountCreateParams.Capabilities.Transfers.builder()
                                    .setRequested(true)
                                    .build()
                            )
                            .build()
                    )
                    .build();
    
                Account account = Account.create(params);
    
                userRepo.updateStripeAccountId(userId, account.getId());
    
                AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(account.getId())
                    .setRefreshUrl(refreshUrl)
                    .setReturnUrl(returnUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();
                AccountLink acctLink = AccountLink.create(linkParams);
    
                return ResponseEntity.ok(
                    Map.of(
                        "message", "Stripe account created",
                        "onboardingUrl", acctLink.getUrl()
                    )
                );
            } catch (StripeException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating Stripe account", "error", e.getMessage()));
            }
        }



    }