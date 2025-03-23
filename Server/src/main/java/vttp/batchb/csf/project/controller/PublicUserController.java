package vttp.batchb.csf.project.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vttp.batchb.csf.project.models.User;
import vttp.batchb.csf.project.repository.UserInterestTagRepository;
import vttp.batchb.csf.project.repository.UserRepository;

@RestController
@RequestMapping("/api/public/user")
public class PublicUserController {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserInterestTagRepository tagRepo;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getPublicUser(@PathVariable String userId) {
        User user1 = userRepo.findByUserId(userId);
        Optional<User> opt = Optional.ofNullable(user1);

        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "User not found"));
        }

        List<String> tags = tagRepo.findTagsForUser(userId);


        User user = opt.get();
        Map<String, Object> publicProfile = new HashMap<>();
        publicProfile.put("userId", user.getUserId());
        publicProfile.put("username", user.getUsername());
        publicProfile.put("profilePicUrl", user.getProfilePictureUrl());
        publicProfile.put("preferredMeetingLocation", user.getPreferredMeetingLocation());
        publicProfile.put("tags", tags);
        

        return ResponseEntity.ok(publicProfile);
    }
}
