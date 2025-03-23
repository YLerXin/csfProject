create database project;

use project;

CREATE TABLE user (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    profile_picture_url VARCHAR(255),
    last_active DATETIME,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE commentsratings (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    reviewer_id VARCHAR(50) NOT NULL,
    reviewer_username VARCHAR(50) NOT NULL,
    comment TEXT,
    rating INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (reviewer_id) REFERENCES user(user_id)
);
CREATE TABLE user_interest_tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    tag VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

ALTER TABLE user 
ADD COLUMN preferred_meeting_location VARCHAR(255);

ALTER TABLE user ADD COLUMN stripe_account_id VARCHAR(50);

