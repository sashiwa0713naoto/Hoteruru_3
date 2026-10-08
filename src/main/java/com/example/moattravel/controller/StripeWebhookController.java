package com.example.moattravel.controller; 

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.moattravel.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@Controller // コントローラークラスであることをSpring Bootに登録
public class StripeWebhookController {

    private final StripeService stripeService;
   
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // 署名検証に失敗した場合は不正なリクエストとみなし、HTTP 400 Bad Request を返却して処理を中断
            return ResponseEntity.status(400).body("Webhook signature verification failed.");
        }

            
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if (session != null) {
                stripeService.completeSession(session);
            }

        return ResponseEntity.status(200).body("Success");
    }
}