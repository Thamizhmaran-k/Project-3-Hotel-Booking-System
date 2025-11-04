package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.dto.BookingDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.User;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    /**
     * Attempts to create a new booking for the given room and dates.
     */
    BookingDto createBooking(Long roomId, User user, LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * Finds all bookings made by a specific user.
     */
    List<BookingDto> findBookingsByUserId(Long userId);
    
    /**
     * Finds a booking by its ID.
     */
    Booking findBookingById(Long bookingId);

    /**
     * NEW: Cancels a booking for a user.
     * Checks if the user is authorized to cancel this booking.
     * @param bookingId The ID of the booking to cancel.
     * @param user The user requesting the cancellation.
     * @throws EntityNotFoundException if the booking doesn't exist or doesn't belong to the user.
     * @throws IllegalStateException if the booking cannot be cancelled (e.g., already cancelled).
     */
    void cancelBooking(Long bookingId, User user);
}