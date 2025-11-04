package com.booking.hotelbookingsystem.repository;

import com.booking.hotelbookingsystem.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds bookings for a specific room that overlap with the given date range.
     */
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.status = 'CONFIRMED' AND " +
           "b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                                          @Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);
    
    /**
     * Finds all bookings for a specific user, ordered by the newest booking first.
     */
    List<Booking> findByUserIdOrderByIdDesc(Long userId);

    /**
     * NEW: Finds a booking by its ID and the ID of the user who owns it.
     * This is crucial for security to prevent one user from cancelling another's booking.
     */
    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);
}