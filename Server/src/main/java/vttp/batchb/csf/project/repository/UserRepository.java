package vttp.batchb.csf.project.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import vttp.batchb.csf.project.repository.Query;
import vttp.batchb.csf.project.models.User;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Boolean insertUser(User user){
        int updated = jdbcTemplate.update(Query.SQL_INSERT_USER,
        user.getUserId(),
        user.getUsername(),
        user.getEmail(),
        user.getProfilePictureUrl(),
        user.getLastActive(),
        user.getPasswordHash(),
        user.getPreferredMeetingLocation(),
        user.getStripe_account_id()

        );
        return updated > 0;
    }

    public User findByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(
                Query.SQL_FIND_BY_EMAIL,
                new RowMapper<User>() {
                    @Override
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User u = new User();
                        u.setUserId(rs.getString("user_id"));
                        u.setUsername(rs.getString("username"));
                        u.setEmail(rs.getString("email"));
                        u.setProfilePictureUrl(rs.getString("profile_picture_url"));
                        if (rs.getTimestamp("last_active") != null) {
                            u.setLastActive(rs.getTimestamp("last_active").toLocalDateTime());
                        }
                        u.setPasswordHash(rs.getString("password_hash"));
                        u.setPreferredMeetingLocation(rs.getString("preferred_meeting_location"));

                        return u;
                    }
                },
                email 
            );
        } catch (Exception e) {
            return null;
        }
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(Query.SQL_EXISTS_BY_EMAIL, Integer.class, email);
        return (count != null && count > 0);
    }
    
    public boolean updateProfilePicture(String userId, String newUrl) {
        int updated = jdbcTemplate.update(Query.SQL_UPDATE_PROFILE_PIC, newUrl, userId);
        return updated > 0;
    }

    public void insertTags(String userId, List<String> tags) {
        for (String t : tags) {
            jdbcTemplate.update("INSERT INTO user_interest_tag (user_id, tag) VALUES (?, ?)",
                userId, t);
        }
    }
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                Query.SQL_EXISTS_BY_USERNAME, Integer.class, username
        );
        return (count != null && count > 0);
    }
    public User findByUserId(String userId) {
    try {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM user WHERE user_id = ?",
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User u = new User();
                    u.setUserId(rs.getString("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setProfilePictureUrl(rs.getString("profile_picture_url"));
                    u.setPreferredMeetingLocation(rs.getString("preferred_meeting_location"));
                    u.setStripe_account_id(rs.getString("stripe_account_id"));

                    return u;
                }
            },
            userId
        );
    } catch (EmptyResultDataAccessException e) {
        return null;
    }
}
String bucketName = "csfprojectlx";
String endPoint = "sgp1.digitaloceanspaces.com";

public String getOldProfilePicKey(String userId) {
    User user = findByUserId(userId);
    if (user == null) return null;
    String oldUrl = user.getProfilePictureUrl();
    if (oldUrl == null || oldUrl.isBlank()) return null;
    
    String prefix = "https://"+bucketName+"."+endPoint+"/";
    if (oldUrl.startsWith(prefix)) {
        return oldUrl.substring(prefix.length());
    }
    return null;
}

public boolean updatePreferredLocation(String userId, String loc) {
    String sql = "UPDATE user SET preferred_meeting_location=? WHERE user_id=?";
    int updated = jdbcTemplate.update(sql, loc, userId);
    return updated > 0;
}

public boolean updateStripeAccountId(String userId, String stripeAcctId) {
    String sql = "UPDATE user SET stripe_account_id=? WHERE user_id=?";
    int updated = jdbcTemplate.update(sql, stripeAcctId, userId);
    return updated > 0;
}
}
