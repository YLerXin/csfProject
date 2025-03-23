package vttp.batchb.csf.project.repository;

public class Query {

    protected static final String SQL_INSERT_USER = 
        "INSERT INTO user (user_id,username, email, profile_picture_url, last_active, password_hash,preferred_meeting_location, stripe_account_id) "
      + "VALUES (?,?, ?, ?, ?, ?, ?,?)";

    protected static final String SQL_FIND_BY_EMAIL =
        "SELECT * FROM user WHERE email = ?";

    protected static final String SQL_EXISTS_BY_EMAIL =
        "SELECT COUNT(*) FROM user WHERE email = ?";

    protected static final String SQL_INSERT_COMMENT = 
        "INSERT INTO commentsratings (user_id, reviewer_id, comment, rating) VALUES (?, ?, ?, ?)";

    protected static final String SQL_FIND_BY_USER_ID = 
        "SELECT * FROM commentsratings WHERE user_id = ? ORDER BY created_at DESC";

    protected static final String SQL_UPDATE_PROFILE_PIC =
        "UPDATE user SET profile_picture_url = ? WHERE user_id = ?";

    protected static final String SQL_INSERT_TAG =
        "INSERT INTO user_interest_tag (user_id, tag) VALUES (?, ?)";

    protected static final String SQL_FIND_TAGS_FOR_USER =
        "SELECT tag FROM user_interest_tag WHERE user_id = ?";

    protected static final String SQL_FIND_USERNAME_IN_COMRAT = 
        "SELECT username FROM user WHERE user_id = ?";

    protected static final String SQL_EXISTS_BY_USERNAME =
        "SELECT COUNT(*) FROM user WHERE username = ?";

    }
