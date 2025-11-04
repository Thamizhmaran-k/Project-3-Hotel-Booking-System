package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.BookingDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Payment; // Import Payment
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.repository.BookingRepository;
import com.booking.hotelbookingsystem.repository.PaymentRepository; // Import PaymentRepository
import com.booking.hotelbookingsystem.repository.RoomRepository;
import com.booking.hotelbookingsystem.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Import Optional
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository; // <-- Inject PaymentRepository

    public BookingServiceImpl(BookingRepository bookingRepository,
                              RoomRepository roomRepository,
                              PaymentRepository paymentRepository) { // <-- Add to constructor
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository; // <-- Initialize
    }

    @Override
    @Transactional
    public BookingDto createBooking(Long roomId, User user, LocalDate checkInDate, LocalDate checkOutDate) {
        
        // --- THIS IS THE FIX: DATE VALIDATION IS FIRST ---
        // 1. Validate inputs (basic)
        if (checkInDate == null || checkOutDate == null || checkOutDate.isBefore(checkInDate) || checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid booking dates provided.");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        // --- END FIX ---

        // 2. Find the room (NOW SECOND)
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Room ID: " + roomId));

        // 3. Check Availability (CRITICAL STEP)
        boolean isAvailable = isRoomAvailable(roomId, checkInDate, checkOutDate);
        if (!isAvailable) {
            throw new IllegalStateException("Sorry, the room is not available for the selected dates.");
        }

        // 4. Create and Save Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setStatus("CONFIRMED"); 

        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByIdDesc(userId);
        return bookings.stream()
                .map(this::mapToBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null); // Return null if not found
    }

    // --- THIS IS THE MISSING METHOD ---
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, User user) {
        // 1. Find the booking AND verify the user owns it
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found or you are not authorized to cancel it."));

        // 2. Check if it can be cancelled
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalStateException("This booking has already been cancelled.");
        }
        
        // 3. Update status
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // 4. Update associated payment status to "REFUND_PENDING"
        Optional<Payment> paymentOpt = paymentRepository.findByBooking(booking);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setPaymentStatus("REFUND_PENDING"); // Or "CANCELLED"
            paymentRepository.save(payment);
        }
    }


    // --- Helper Methods ---

    private boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(roomId, checkInDate, checkOutDate);
        return overlappingBookings.isEmpty();
    }

    private BookingDto mapToBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setStatus(booking.getStatus());

        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserName(booking.getUser().getName());
        }
        if (booking.getRoom() != null) {
            dto.setRoomId(booking.getRoom().getId());
            dto.setRoomType(booking.getRoom().getRoomType());
            dto.setPricePerNight(booking.getRoom().getPricePerNight());
            if (booking.getRoom().getHotel() != null) {
                dto.setHotelId(booking.getRoom().getHotel().getId());
                dto.setHotelName(booking.getRoom().getHotel().getName());
            }
        }
        return dto;
    }
}