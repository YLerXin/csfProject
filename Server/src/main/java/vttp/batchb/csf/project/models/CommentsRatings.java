package vttp.batchb.csf.project.models;

import java.time.LocalDateTime;

public class CommentsRatings {
    private int commentId;
    private String userId; 
    private String reviewerId;
    private String comment;
    private Integer rating;
    private LocalDateTime createdAt;

    public int getCommentId() {
        return commentId;
    }
    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReviewerId() {
        return reviewerId;
    }
    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
