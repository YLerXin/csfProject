package vttp.batchb.csf.project.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;

public class Product {

    private ObjectId id;

    
    private String productId;    
    private String userId;        
    private String productName;
    private String productDetails;
    private boolean availability;
    private String condition;   
    private LocalDateTime dateAdded;
    private BigDecimal price;
    private List<String> images;  
    private List<String> tags;
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getProductDetails() {
        return productDetails;
    }
    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }
    public boolean isAvailability() {
        return availability;
    }
    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
    public LocalDateTime getDateAdded() {
        return dateAdded;
    }
    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }

    
}
