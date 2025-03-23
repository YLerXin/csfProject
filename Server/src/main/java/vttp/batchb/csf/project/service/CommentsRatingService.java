package vttp.batchb.csf.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vttp.batchb.csf.project.models.CommentsRatings;
import vttp.batchb.csf.project.repository.CommentsRatingRepository;

@Service
public class CommentsRatingService {

    private final CommentsRatingRepository commentsRepo;

    public CommentsRatingService(CommentsRatingRepository commentsRepo) {
        this.commentsRepo = commentsRepo;
    }

    public boolean insertCommentRating(CommentsRatings cr) {
        return commentsRepo.insertCommentRating(cr);
    }

    public List<CommentsRatings> findByUserId(int userId) {
        return commentsRepo.findByUserId(userId);
    }
}
