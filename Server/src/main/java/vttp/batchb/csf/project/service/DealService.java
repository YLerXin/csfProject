package vttp.batchb.csf.project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import vttp.batchb.csf.project.models.Deal;
import vttp.batchb.csf.project.models.DealMessage;
import vttp.batchb.csf.project.models.Product;
import vttp.batchb.csf.project.models.User;
import vttp.batchb.csf.project.repository.DealRepository;
import vttp.batchb.csf.project.repository.ProductRepository;

@Service
public class DealService {

    @Autowired
    private DealRepository dealRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private UserService userService;

    public Deal createDeal(Deal deal) {
        deal.setInitiatorAccepted(false);
        deal.setOwnerAccepted(false);
        deal.setRejected(false);
        deal.setCompleted(false);
        deal.setPendingPayment(false);

        recalcDifference(deal);
        return dealRepo.saveDeal(deal);
    }

    public Deal findById(String dealId) {
        return dealRepo.findDealById(dealId);
    }

    public Deal updateDealItems(String dealId, List<Product> initiatorItems, List<Product> ownerItems) {
        Deal d = dealRepo.findDealById(dealId);
        if (d == null) return null;
        if (d.isCompleted() || d.isRejected() || d.isPendingPayment()) {
            return null;
        }
        d.setInitiatorAccepted(false);
        d.setOwnerAccepted(false);

        if (initiatorItems != null) {
            d.setInitiatorItems(initiatorItems);
        }
        if (ownerItems != null) {
            d.setOwnerItems(ownerItems);
        }
        recalcDifference(d);
     
        messagingTemplate.convertAndSend("/topic/deals", d);

        return dealRepo.saveDeal(d);
    }

    public Deal postMessage(String dealId, String senderId, String text) {
        Deal d = dealRepo.findDealById(dealId);
        if (d == null) return null;
        if (d.isCompleted() || d.isRejected()) return d; 

        DealMessage msg = new DealMessage();
        msg.setSenderId(senderId);
        msg.setText(text);
        msg.setTimestamp(LocalDateTime.now());

        User user = userService.getUserById(senderId);
        if (user != null) {
          msg.setSenderUsername(user.getUsername());
        } else {
          msg.setSenderUsername(senderId); 
        }

        d.getMessages().add(msg);

        d.setLastAction("MESSAGE");


        Deal updated = dealRepo.saveDeal(d);


        messagingTemplate.convertAndSend("/topic/deals", updated);

        return updated;
    }


    public void completeDeal(Deal d) {
        d.setCompleted(true);
        for (Product p : d.getInitiatorItems()) {
            productRepo.setAvailabilityByProductId(p.getProductId(), false);
        }
        for (Product p : d.getOwnerItems()) {
            productRepo.setAvailabilityByProductId(p.getProductId(), false);
        }
    }

    private void revertAvailability(Deal d) {
        for (Product p : d.getInitiatorItems()) {
            productRepo.setAvailabilityByProductId(p.getProductId(), true);
        }
        for (Product p : d.getOwnerItems()) {
            productRepo.setAvailabilityByProductId(p.getProductId(), true);
        }
    }

    private void recalcDifference(Deal d) {
        BigDecimal sumInitiator = d.getInitiatorItems().stream()
            .map(Product::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal sumOwner = d.getOwnerItems().stream()
            .map(Product::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
            d.setFinalPriceDifference(sumOwner.subtract(sumInitiator));
        }

    public Deal updateMeeting(String dealId, String location, LocalDateTime dateTime) {
        Deal d = dealRepo.findDealById(dealId);
        if (d == null) return null;
        if (d.isCompleted() || d.isRejected()|| d.isPendingPayment()) return d;
        
        d.setInitiatorAccepted(false);
        d.setOwnerAccepted(false);
    
        d.setMeetingLocation(location);
        d.setMeetingDateTime(dateTime);

        messagingTemplate.convertAndSend("/topic/deals", d);


        return dealRepo.saveDeal(d);
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Deal acceptFromUser(String dealId, String userId) {
        Deal d = dealRepo.findDealById(dealId);
        if (d == null) return null;
        if (d.isCompleted() || d.isRejected()) {
            return d;
        }

        if (userId.equals(d.getInitiatorId())) {
            d.setInitiatorAccepted(true);
        } else if (userId.equals(d.getOwnerId())) {
            d.setOwnerAccepted(true);
        }
        d = dealRepo.saveDeal(d);

        if (d.isInitiatorAccepted() && d.isOwnerAccepted()) {
        if (d.getFinalPriceDifference().compareTo(BigDecimal.ZERO) > 0) {
            PaymentIntent intent = createStripePaymentIntent(d.getFinalPriceDifference());
            d.setPendingPayment(true);        
            d.setPaymentIntentId(intent.getId()); 
            d = dealRepo.saveDeal(d);
        } else {
            completeDeal(d);
            d = dealRepo.saveDeal(d);
        }
    }
        User user = userService.getUserById(userId);
        String username = (user != null) ? user.getUsername() : userId;
        d.setLastUpdatedBy(username);
        d = dealRepo.saveDeal(d);
        messagingTemplate.convertAndSend("/topic/deals", d);
        return d;
    }
    public List<Deal> findDealsForUser(String userId) {
        return dealRepo.findDealsForUser(userId);
    }

    private PaymentIntent createStripePaymentIntent(BigDecimal finalPrice) {
        long amountInCents = finalPrice
        .multiply(BigDecimal.valueOf(100))
        .longValue();
    
        PaymentIntentCreateParams createParams = PaymentIntentCreateParams
        .builder()
        .setAmount(Long.valueOf(amountInCents)) 
        .setCurrency("sgd")                 
        .build();

    try {
        return PaymentIntent.create(createParams);
    } catch (StripeException e) {
        throw new RuntimeException("Failed to create payment intent", e);
    }
}


    public Deal rejectDeal(String dealId, String userId) {
        Deal d = dealRepo.findDealById(dealId);
        if (d == null) return null;
        if (d.isCompleted()) {
            // done
            return d;

        }
        d.setRejected(true);
        d.setInitiatorAccepted(false);
        d.setOwnerAccepted(false);
        d.setCompleted(false);

        if (d.isPendingPayment() && d.getPaymentIntentId() != null) {
            try {
                PaymentIntent intent = PaymentIntent.retrieve(d.getPaymentIntentId());
                intent.cancel();
            } catch (StripeException e) {
                System.out.println(e);
            }
            d.setPendingPayment(false);
            d.setPaymentIntentId(null);
        }
        User user = userService.getUserById(userId);
        String username = user != null ? user.getUsername() : userId;
        d.setLastUpdatedBy(username);

        d = dealRepo.saveDeal(d);
        revertAvailability(d);
        messagingTemplate.convertAndSend("/topic/deals", d);
        return d;
    }

    public void broadcastDealUpdate(Deal d) {
        messagingTemplate.convertAndSend("/topic/deals", d);
    }




}
