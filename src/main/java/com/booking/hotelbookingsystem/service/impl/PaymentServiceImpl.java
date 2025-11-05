package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment;
import com.booking.hotelbookingsystem.repository.BookingRepository;
import com.booking.hotelbookingsystem.repository.PaymentRepository;
import com.booking.hotelbookingsystem.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository; 

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public Payment processPayment(Booking booking, BigDecimal amount) {
        if (booking == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid booking or amount for payment.");
        }
        String simulatedTransactionId = "txn_" + UUID.randomUUID().toString().substring(0, 10);
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionId(simulatedTransactionId);
        
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    // --- ADD THIS METHOD ---
    @Override
    public long countSuccessfulPayments() {
        return paymentRepository.countByPaymentStatus("SUCCESS");
    }
}