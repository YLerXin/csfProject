package vttp.batchb.csf.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import vttp.batchb.csf.project.models.User;
import vttp.batchb.csf.project.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String createUserReturnId(String username, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            return null;
        }

        String userId = UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 10);

        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setLastActive(LocalDateTime.now());

        boolean success = userRepository.insertUser(user);
        if (!success) {
            return null;
        }
        return userId;
    }


    public boolean validateLogin(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public User getUser(String email){
        return userRepository.findByEmail(email);
    }
    public User getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    
}
