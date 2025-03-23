package vttp.batchb.csf.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vttp.batchb.csf.project.models.CommentsRatings;
import vttp.batchb.csf.project.service.CommentsRatingService;

@RestController
@RequestMapping("/api/comments")
public class CommentsRatingController {

    private final CommentsRatingService commentsService;

    public CommentsRatingController(CommentsRatingService commentsService) {
        this.commentsService = commentsService;
    }


    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentsRatings request) {
        boolean inserted = commentsService.insertCommentRating(request);
        if (!inserted) {
            return ResponseEntity.badRequest().body("Could not insert comment/rating");
        }
        return ResponseEntity.ok("Comment/rating added");
    }

    @GetMapping("/{userId}")
    public List<CommentsRatings> getCommentsForUser(@PathVariable int userId) {
        return commentsService.findByUserId(userId);
    }
}
