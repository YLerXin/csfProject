package vttp.batchb.csf.project.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import vttp.batchb.csf.project.models.CommentsRatings;

@Repository
public class CommentsRatingRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public boolean insertCommentRating(CommentsRatings cr) {
        int updated = jdbcTemplate.update(Query.SQL_INSERT_COMMENT,
            cr.getUserId(),
            cr.getReviewerId(),
            cr.getComment(),
            cr.getRating()
        );
        return updated > 0;
    }
    public List<CommentsRatings> findByUserId(int userId) {
        return jdbcTemplate.query(Query.SQL_FIND_BY_USER_ID, new Object[]{userId}, new RowMapper<CommentsRatings>() {
            @Override
            public CommentsRatings mapRow(ResultSet rs, int rowNum) throws SQLException {
                CommentsRatings c = new CommentsRatings();
                c.setCommentId(rs.getInt("comment_id"));
                c.setUserId(rs.getString("user_id"));
                c.setReviewerId(rs.getString("reviewer_id"));
                c.setComment(rs.getString("comment"));
                c.setRating(rs.getInt("rating"));
                if(rs.getTimestamp("created_at") != null) {
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                } else {
                    c.setCreatedAt(LocalDateTime.now());
                }
                return c;
            }
        });
    }
}
