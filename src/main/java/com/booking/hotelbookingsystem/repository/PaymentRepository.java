package com.booking.hotelbookingsystem.repository;

import com.booking.hotelbookingsystem.model.Booking; // Import Booking
import com.booking.hotelbookingsystem.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Import Optional

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    long countByPaymentStatus(String paymentStatus);

    /**
     * NEW: Finds a Payment record associated with a specific Booking.
     */
    Optional<Payment> findByBooking(Booking booking);
}