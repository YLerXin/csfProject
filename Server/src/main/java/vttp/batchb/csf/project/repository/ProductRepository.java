package vttp.batchb.csf.project.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import vttp.batchb.csf.project.models.Product;

@Repository
public class ProductRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "products";

    public Product createProduct(Product p) {
        System.out.println("\ncreateProduct called with p.productId=" + p.getProductId());
    
        if (p.getProductId() == null || p.getProductId().isEmpty()) {
            p.setProductId(UUID.randomUUID().toString());
            System.out.println("No productId found. Generated new: " + p.getProductId());
        }
    
        if (p.getDateAdded() == null) {
            p.setDateAdded(LocalDateTime.now());
        }
    
        Product existing = findByProductId(p.getProductId());

        System.out.println("Did we find existing doc? " + (existing != null));
    
        if (existing != null) {
            System.out.println("Updating existing doc with productId=" + existing.getProductId());
            existing.setProductName(p.getProductName());
            existing.setProductDetails(p.getProductDetails());
            existing.setCondition(p.getCondition());
            existing.setPrice(p.getPrice());
            existing.setAvailability(p.isAvailability());
            existing.setImages(p.getImages());
            existing.setTags(p.getTags());
    
            Product updated = mongoTemplate.save(existing, COLLECTION_NAME);
            System.out.println("Updated doc. Returning updated product.");
            return updated;
        } else {
            System.out.println("No existing doc found. Creating brand-new doc with availability=true");
            p.setAvailability(true);
    
            Product inserted = mongoTemplate.save(p, COLLECTION_NAME);
            System.out.println("Inserted new doc with productId=" + inserted.getProductId());
            return inserted;
        }
    }


    public List<Product> findAll() {
        return mongoTemplate.findAll(Product.class, COLLECTION_NAME);
    }

    public Product findById(String id) {
        return mongoTemplate.findById(id, Product.class, COLLECTION_NAME);
    }

    public Product updateProduct(Product p) {
        return mongoTemplate.save(p, COLLECTION_NAME); 
    }

    public void deleteProduct(String id) {
        Product p = this.findById(id);
        if (p != null) {
            mongoTemplate.remove(p, COLLECTION_NAME);
        }
    }
public List<Product> searchByNameOrTags(String keyword) {
    AggregationOperation matchOp = Aggregation.match(
        new Criteria().orOperator(
            Criteria.where("productName").regex(keyword, "i"), 
            Criteria.where("tags").in(keyword)             
        )
    );
    AggregationOperation limitOp = Aggregation.limit(6);

    Aggregation pipeline = Aggregation.newAggregation(matchOp,limitOp);

    AggregationResults<Product> results =
        mongoTemplate.aggregate(pipeline, COLLECTION_NAME, Product.class);

    return results.getMappedResults();
}

public Product findByProductId(String productId) {
    System.out.println("findingByProductID");
    AggregationOperation matchOp = Aggregation.match(Criteria.where("productId").is(productId));
    AggregationOperation limitOp = Aggregation.limit(1);
    Aggregation pipeline = Aggregation.newAggregation(matchOp, limitOp);
    AggregationResults<Product> results =
        mongoTemplate.aggregate(pipeline, COLLECTION_NAME, Product.class);
        System.out.println(results);


    return results.getUniqueMappedResult();
}

public List<Product> searchByNameOrTagsAndAvailability(String keyword) {
    AggregationOperation matchOp = Aggregation.match(
        new Criteria().andOperator(
            Criteria.where("availability").is(true),
            new Criteria().orOperator(
                Criteria.where("productName").regex(keyword, "i"),
                Criteria.where("tags").elemMatch(Criteria.where("$regex").is(keyword).and("$options").is("i"))
                )
        )
    );
    Aggregation pipeline = Aggregation.newAggregation(matchOp);
    AggregationResults<Product> results =
        mongoTemplate.aggregate(pipeline, COLLECTION_NAME, Product.class);
    return results.getMappedResults();
}

public void setAvailabilityByProductId(String productId, boolean isAvailable) {
    Product p = findByProductId(productId);
    System.out.println("setAvailabilityByProductId found product? = " + (p != null));

    if (p != null) {
        System.out.println("p.getId() = " + p.getId()); 

        p.setAvailability(isAvailable);
        mongoTemplate.save(p, COLLECTION_NAME);
        System.out.println("Updated product " + productId + " to isAvailability=" + isAvailable);
    }
}

}