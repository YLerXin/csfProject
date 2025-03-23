package vttp.batchb.csf.project.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import vttp.batchb.csf.project.repository.UserRedisRepository;
import vttp.batchb.csf.project.service.UserService;
import vttp.batchb.csf.project.models.AuthRequest;
import vttp.batchb.csf.project.models.User;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final UserRedisRepository redisRepo;

    public AuthController(UserService userService, UserRedisRepository redisRepo) {
        this.userService = userService;
        this.redisRepo = redisRepo;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> handleSignup(@RequestBody AuthRequest request) {
        String newUserId = userService.createUserReturnId(request.getUsername(), request.getEmail(), request.getPassword());
        if (newUserId == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "User already exists"));
        }
        return ResponseEntity.ok(Map.of(
            "message", "Account created",
            "userId", newUserId
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(@RequestBody AuthRequest request, HttpSession session) {
        boolean valid = userService.validateLogin(request.getEmail(), request.getPassword());
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        session.setAttribute("loggedInUser", request.getEmail());
        redisRepo.saveSession(request.getEmail(), "SESSION_TOKEN_" + request.getEmail());

        User user = userService.getUser(request.getEmail());

        if(user ==null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found");
        }
        Map<String, Object> userMap = Map.of(
            "userId", user.getUserId() != null ? user.getUserId() : "",
            "username", user.getUsername() != null ? user.getUsername() : "",
            "email", user.getEmail() != null ? user.getEmail() : "",
            "profilePicUrl", user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : ""
        );
        Map<String, Object> responseBody = Map.of("user", userMap);
    
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> handleLogout(HttpSession session) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email != null) {
            redisRepo.removeSession(email);
        }
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}