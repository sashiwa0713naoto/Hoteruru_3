package com.example.moattravel.service;

import java.time.LocalDate;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moattravel.entity.Reservation;
import com.example.moattravel.form.ReservationRegisterForm;
import com.example.moattravel.repository.HouseRepository;
import com.example.moattravel.repository.ReservationRepository;
import com.example.moattravel.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class StripeService {

    private final ReservationRepository reservationRepository;
    private final HouseRepository houseRepository;
    private final UserRepository userRepository;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    public StripeService(ReservationRepository reservationRepository, HouseRepository houseRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
    }

    public String createStripeSession(String houseName, ReservationRegisterForm reservationRegisterForm, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        
        String requestUrl = new String(httpServletRequest.getRequestURL());
        String baseUrl = requestUrl.replace(httpServletRequest.getRequestURI(), "");

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("jpy")
                                .setUnitAmount((long) reservationRegisterForm.getAmount())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(houseName)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/reservations?reserved&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/houses/" + reservationRegisterForm.getHouseId())
                .putMetadata("houseId", String.valueOf(reservationRegisterForm.getHouseId()))
                .putMetadata("userId", String.valueOf(reservationRegisterForm.getUserId()))
                .putMetadata("checkinDate", reservationRegisterForm.getCheckinDate())
                .putMetadata("checkoutDate", reservationRegisterForm.getCheckoutDate())
                .putMetadata("numberOfPeople", String.valueOf(reservationRegisterForm.getNumberOfPeople()))
                .putMetadata("amount", String.valueOf(reservationRegisterForm.getAmount()))
                .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            System.err.println("Stripe API Exception: " + e.getMessage());
            return "";
        }
    }

    @Transactional
    public void completeSession(Session session) {
        Map<String, String> metadata = session.getMetadata();

        Integer houseId = Integer.valueOf(metadata.get("houseId"));
        Integer userId = Integer.valueOf(metadata.get("userId"));
        LocalDate checkinDate = LocalDate.parse(metadata.get("checkinDate"));
        LocalDate checkoutDate = LocalDate.parse(metadata.get("checkoutDate"));
        Integer numberOfPeople = Integer.valueOf(metadata.get("numberOfPeople"));
        Integer amount = Integer.valueOf(metadata.get("amount"));

        Reservation reservation = new Reservation();
        
        reservation.setHouse(houseRepository.getReferenceById(houseId));
        reservation.setUser(userRepository.getReferenceById(userId));
        
        reservation.setCheckinDate(checkinDate);
        reservation.setCheckoutDate(checkoutDate);
        reservation.setNumberOfPeople(numberOfPeople);
        reservation.setAmount(amount);

        reservationRepository.save(reservation);
    }

    @Transactional
    public void createReservation(String sessionId) {
        try {
            Stripe.apiKey = stripeApiKey;
            Session session = Session.retrieve(sessionId);
            completeSession(session);
        } catch (StripeException e) {
            System.err.println("Stripe API Exception (Create Reservation): " + e.getMessage());
        }
     }
}