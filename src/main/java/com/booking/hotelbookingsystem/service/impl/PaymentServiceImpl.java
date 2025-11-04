package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment;
import com.booking.hotelbookingsystem.repository.PaymentRepository;
import com.booking.hotelbookingsystem.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID; // To generate a fake transaction ID

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Payment processPayment(Booking booking, BigDecimal amount) {
        if (booking == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid booking or amount for payment.");
        }

        // --- Payment Simulation ---
        // In a real app, you'd interact with Stripe/Razorpay here.
        // We'll just assume it's successful for this simulation.
        boolean paymentSuccessful = true; 
        String simulatedTransactionId = "txn_" + UUID.randomUUID().toString().substring(0, 10);

        // --- Record Payment ---
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(paymentSuccessful ? "SUCCESS" : "FAILED");
        payment.setTransactionId(simulatedTransactionId);

        return paymentRepository.save(payment);
    }
}