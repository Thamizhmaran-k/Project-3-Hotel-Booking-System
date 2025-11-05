package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.dto.BookingDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.User;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long roomId, User user, LocalDate checkInDate, LocalDate checkOutDate);
    List<BookingDto> findBookingsByUserId(Long userId);
    Booking findBookingById(Long bookingId);
    void cancelBooking(Long bookingId, User user);

    // --- ADD THIS METHOD ---
    long countBookings();
}