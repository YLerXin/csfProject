package vttp.batchb.csf.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vttp.batchb.csf.project.models.Deal;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserService userService;

    
    public EmailService(JavaMailSender mailSender,UserService userService) {
        this.mailSender = mailSender;
        this.userService = userService;

    }

    public void sendDealCompletionEmail(String toEmail, Deal deal) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Thank you for your barter!");
            String text = buildBody(deal);
            helper.setText(text, true);

            mailSender.send(message);
            System.out.println("Email sent to: " + toEmail);

        } catch (MailException mailEx) {
            System.err.println("MailException occurred: " + mailEx.getMessage());
            mailEx.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Value("${app.barterUrl}")
    private String barterUrl;
    private String buildBody(Deal deal) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Thank You for Your Barter!</h1>");
        sb.append("<p>Your deal has been completed.</p>");
        sb.append("<p><strong>Deal ID:</strong> ").append(deal.getId()).append("</p>");

        sb.append("<p>Owner Items:</p><ul>");
        deal.getOwnerItems().forEach(item ->
            sb.append("<li>").append(item.getProductName())
              .append(" ($").append(item.getPrice()).append(")")
              .append("</li>")
        );
        sb.append("</ul>");

        sb.append("<p>Initiator Items:</p><ul>");
        deal.getInitiatorItems().forEach(item ->
            sb.append("<li>").append(item.getProductName())
              .append(" ($").append(item.getPrice()).append(")")
              .append("</li>")
        );
        sb.append("</ul>");

        sb.append("<p>You can view the details here: ")
        //Change in railway
        .append("<a href='").append(barterUrl).append("/")
        .append(deal.getId()).append("'>View Deal</a></p>");

        sb.append("<p>Thank you for using our service!</p>");

        String ownerEmail = "";
        String initiatorEmail = "";
        try {
            var owner = userService.getUserById(deal.getOwnerId());
            if (owner != null && owner.getEmail() != null) {
                ownerEmail = owner.getEmail();
            }
            var initiator = userService.getUserById(deal.getInitiatorId());
            if (initiator != null && initiator.getEmail() != null) {
                initiatorEmail = initiator.getEmail();
            }
        } catch (Exception e) {
        }

        if (!ownerEmail.isEmpty() && !initiatorEmail.isEmpty()) {
            sb.append("<h3>Contact Information</h3>")
              .append("<p><strong>Owner’s Email:</strong> ")
              .append(ownerEmail)
              .append("</p>")
              .append("<p><strong>Initiator’s Email:</strong> ")
              .append(initiatorEmail)
              .append("</p>");
        }

        return sb.toString();
    }
}
