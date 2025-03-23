package vttp.batchb.csf.project.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import vttp.batchb.csf.project.models.Deal;

@Repository
public class DealRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "deals";

    public Deal findDealById(String dealId) {
        ObjectId objId;
        try {
            objId = new ObjectId(dealId);
        } catch (Exception e) {
            return null;
        }
        AggregationOperation match = Aggregation.match(Criteria.where("_id").is(objId));
        Aggregation agg = Aggregation.newAggregation(match);
        AggregationResults<Deal> results =
            mongoTemplate.aggregate(agg, COLLECTION_NAME, Deal.class);
        return results.getUniqueMappedResult();
    }

    public List<Deal> findDealsForUser(String userId) {
        AggregationOperation match = Aggregation.match(
            new Criteria().orOperator(
                Criteria.where("initiatorId").is(userId),
                Criteria.where("ownerId").is(userId)
            )
        );
        Aggregation agg = Aggregation.newAggregation(match);
        AggregationResults<Deal> results =
            mongoTemplate.aggregate(agg, COLLECTION_NAME, Deal.class);
        return results.getMappedResults();
    }

    public Deal saveDeal(Deal deal) {
        return mongoTemplate.save(deal, COLLECTION_NAME);
    }

    public void deleteDeal(String dealId) {
        Deal d = findDealById(dealId);
        if (d != null) {
            mongoTemplate.remove(d, COLLECTION_NAME);
        }
    }
}
