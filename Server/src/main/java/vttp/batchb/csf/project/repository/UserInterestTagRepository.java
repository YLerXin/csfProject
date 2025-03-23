package vttp.batchb.csf.project.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserInterestTagRepository {
    
    @Autowired
    private JdbcTemplate jdbc;


    public void insertTags(String userId, List<String> tags) {
        for (String t : tags) {
            jdbc.update(Query.SQL_INSERT_TAG, userId, t);
        }
    }

    public List<String> findTagsForUser(String userId) {
        return jdbc.queryForList(Query.SQL_FIND_TAGS_FOR_USER,String.class,userId);    }

    public void removeAllTagsForUser(String userId) {
        jdbc.update("DELETE FROM user_interest_tag WHERE user_id=?", userId);
    }
    
}