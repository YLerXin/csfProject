package vttp.batchb.csf.project.models;

import java.time.LocalDateTime;

public class User {
    private String userId;
    private String username;
    private String email;
    private String profilePictureUrl;
    private LocalDateTime lastActive;
    private String passwordHash;
    private String preferredMeetingLocation;
    private String stripe_account_id;

    public String getStripe_account_id() {
		return stripe_account_id;
	}
	public void setStripe_account_id(String stripe_account_id) {
		this.stripe_account_id = stripe_account_id;
	}
	public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPreferredMeetingLocation() {
        return preferredMeetingLocation;
    }
    
    public void setPreferredMeetingLocation(String preferredMeetingLocation) {
        this.preferredMeetingLocation = preferredMeetingLocation;
    }
}
