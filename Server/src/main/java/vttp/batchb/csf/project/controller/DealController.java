package vttp.batchb.csf.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.param.TransferCreateParams;

import vttp.batchb.csf.project.models.Deal;
import vttp.batchb.csf.project.models.DealMessage;
import vttp.batchb.csf.project.models.MeetingRequest;
import vttp.batchb.csf.project.models.UpdateItemRequest;
import vttp.batchb.csf.project.models.User;
import vttp.batchb.csf.project.repository.DealRepository;
import vttp.batchb.csf.project.service.DealService;
import vttp.batchb.csf.project.service.EmailService;
import vttp.batchb.csf.project.service.UserService;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    @Autowired
    private DealService dealSvc;
    @Autowired
    private DealRepository dealRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService mailService;

    @PostMapping("/initiate")
    public ResponseEntity<Deal> initiateDeal(@RequestBody Deal deal) {
        Deal created = dealSvc.createDeal(deal);
        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<Deal> getDealById(@PathVariable String dealId) {
        Deal found = dealSvc.findById(dealId);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{dealId}/items")
    public ResponseEntity<Deal> updateDealItems(
            @PathVariable String dealId,
            @RequestBody UpdateItemRequest req
    ) {
        Deal updated = dealSvc.updateDealItems(
            dealId,
            req.getInitiatorItems(),
            req.getOwnerItems()
        );
        if (updated == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{dealId}/message")
    public ResponseEntity<Deal> postMessage(
        @PathVariable String dealId,
        @RequestBody DealMessage req
    ) {
        Deal updated = dealSvc.postMessage(dealId, req.getSenderId(), req.getText());
        if (updated == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{dealId}/accept")
    public ResponseEntity<Deal> acceptFromUser(
        @PathVariable String dealId,
        @RequestParam String userId
    ) {
        Deal updated = dealSvc.acceptFromUser(dealId, userId);
        if (updated == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{dealId}/reject")
    public ResponseEntity<Deal> rejectDeal(
        @PathVariable String dealId,
        @RequestParam String userId
    ) {
        Deal updated = dealSvc.rejectDeal(dealId, userId);
        if (updated == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{dealId}/meeting")
    public ResponseEntity<Deal> updateMeeting(
        @PathVariable String dealId,
        @RequestBody MeetingRequest req
    ) {
        Deal updated = dealSvc.updateMeeting(dealId, req.getLocation(), req.getDateTime());
        if (updated == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(updated);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Deal>> getDealsForUser(@PathVariable String userId) {
        List<Deal> userDeals = dealSvc.findDealsForUser(userId);
        if (userDeals == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDeals);
    }

    @GetMapping("/{dealId}/payment-intent-secret")
    public ResponseEntity<?> getPaymentIntentSecret(@PathVariable String dealId) {
        Deal d = dealSvc.findById(dealId);
        if (d == null || d.getPaymentIntentId() == null) {
            return ResponseEntity.badRequest().body("No Payment Intent for this deal");
        }
        try {
            PaymentIntent intent = PaymentIntent.retrieve(d.getPaymentIntentId());
            String clientSecret = intent.getClientSecret();
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error retrieving PaymentIntent");
        }
    }
    
    @PostMapping("/{dealId}/confirm-paid")
    public ResponseEntity<?> confirmPaid(@PathVariable String dealId) {
        Deal d = dealSvc.findById(dealId);
        if (d == null || !d.isPendingPayment()) {
            return ResponseEntity.badRequest().body("No pending payment for this deal");
        }
    
        try {
            PaymentIntent intent = PaymentIntent.retrieve(d.getPaymentIntentId());
            if (!"succeeded".equals(intent.getStatus())) {
                return ResponseEntity.badRequest().body("Payment not confirmed on Stripe side");
            }
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error verifying PaymentIntent");
        }
    
        d.setPendingPayment(false);
        d.setPaymentIntentId(null);
        dealSvc.completeDeal(d);
        dealRepo.saveDeal(d);
    
        try {
            String ownerId = d.getOwnerId();
            User owner = userService.getUserById(ownerId);
            String connectedAcctId = owner.getStripe_account_id();
            if (connectedAcctId == null) {
                System.out.println("Owner has no stripe_account_id");
            } else {
                long amountCents = d.getFinalPriceDifference() * 100L;
                
                TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("sgd")
                    .setDestination(connectedAcctId) 
                    .build();
    
                Transfer transfer = Transfer.create(params);
                System.out.println("Created transfer: " + transfer.getId());
            }
        } catch (StripeException e) {
            e.printStackTrace();
        }

        String buyerId = d.getInitiatorId(); 
        User buyer = userService.getUserById(buyerId);
        if (buyer != null && buyer.getEmail() != null) {
            mailService.sendDealCompletionEmail(buyer.getEmail(), d);
        }
        String ownerId = d.getOwnerId();
        User owner = userService.getUserById(ownerId);
        if (owner != null && owner.getEmail() != null) {
            mailService.sendDealCompletionEmail(owner.getEmail(), d);
        }
    
        return ResponseEntity.ok(d);
    }
    


}
