package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment;
import com.booking.hotelbookingsystem.repository.BookingRepository; // Import the missing repository
import com.booking.hotelbookingsystem.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Re-add this import for the test

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    // --- THIS IS THE FIX ---
    @Mock 
    private BookingRepository bookingRepository;
    // --- END FIX ---

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking booking;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setId(1L);
        amount = BigDecimal.valueOf(250.50);
    }

    @Test
    void shouldProcessPaymentAndRecordSuccess() {
        // Arrange
        // We don't need to mock bookingRepository.save(), as its default
        // (returning null) is fine since we don't use the result.
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L); // Simulate ID generation
            return p;
        });

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // Act
        Payment createdPayment = paymentService.processPayment(booking, amount);

        // Assert
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        verify(bookingRepository, times(1)).save(any(Booking.class)); // Verify it was called
        
        Payment savedPayment = paymentCaptor.getValue();

        assertThat(savedPayment.getBooking()).isEqualTo(booking);
        assertThat(savedPayment.getAmount()).isEqualTo(amount);
        assertThat(savedPayment.getPaymentStatus()).isEqualTo("SUCCESS");
        assertThat(createdPayment.getPaymentStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldThrowExceptionForNullBooking() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(null, amount);
        });
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}