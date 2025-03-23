package vttp.batchb.csf.project.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Deal {
    private String id;
    private String initiatorId;
    private String ownerId;

    private boolean initiatorAccepted;
    private boolean ownerAccepted;
    private boolean rejected;
    private boolean completed;

    private List<Product> initiatorItems = new ArrayList<>();
    private List<Product> ownerItems = new ArrayList<>();
    private Integer finalPriceDifference;
    private String meetingLocation;
    private LocalDateTime meetingDateTime;
    private List<DealMessage> messages = new ArrayList<>();

    private boolean pendingPayment;
    private String paymentIntentId;
    
    public boolean isPendingPayment() {
        return pendingPayment;
    }
    public void setPendingPayment(boolean pendingPayment) {
        this.pendingPayment = pendingPayment;
    }
    public String getPaymentIntentId() {
        return paymentIntentId;
    }
    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getInitiatorId() {
        return initiatorId;
    }
    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }
    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public boolean isInitiatorAccepted() {
        return initiatorAccepted;
    }
    public void setInitiatorAccepted(boolean initiatorAccepted) {
        this.initiatorAccepted = initiatorAccepted;
    }
    public boolean isOwnerAccepted() {
        return ownerAccepted;
    }
    public void setOwnerAccepted(boolean ownerAccepted) {
        this.ownerAccepted = ownerAccepted;
    }
    public boolean isRejected() {
        return rejected;
    }
    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public List<Product> getInitiatorItems() {
        return initiatorItems;
    }
    public void setInitiatorItems(List<Product> initiatorItems) {
        this.initiatorItems = initiatorItems;
    }
    public List<Product> getOwnerItems() {
        return ownerItems;
    }
    public void setOwnerItems(List<Product> ownerItems) {
        this.ownerItems = ownerItems;
    }
    public Integer getFinalPriceDifference() {
        return finalPriceDifference;
    }
    public void setFinalPriceDifference(Integer finalPriceDifference) {
        this.finalPriceDifference = finalPriceDifference;
    }
    public String getMeetingLocation() {
        return meetingLocation;
    }
    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = meetingLocation;
    }
    public LocalDateTime getMeetingDateTime() {
        return meetingDateTime;
    }
    public void setMeetingDateTime(LocalDateTime meetingDateTime) {
        this.meetingDateTime = meetingDateTime;
    }
    public List<DealMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<DealMessage> messages) {
        this.messages = messages;
    }
    
}
