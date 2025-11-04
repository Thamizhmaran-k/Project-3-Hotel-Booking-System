package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.BookingDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Hotel;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.model.User;
import com.booking.hotelbookingsystem.repository.BookingRepository;
import com.booking.hotelbookingsystem.repository.PaymentRepository;
import com.booking.hotelbookingsystem.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PaymentRepository paymentRepository; // Mock this

    @InjectMocks private BookingServiceImpl bookingService;

    private User user;
    private Room room;
    private LocalDate checkIn = LocalDate.now().plusDays(1);
    private LocalDate checkOut = LocalDate.now().plusDays(5);
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User(); user.setId(1L); user.setName("Booker"); user.setEmail("booker@test.com");
        Hotel hotel = new Hotel(); hotel.setId(1L); hotel.setName("Bookable Hotel");
        room = new Room(); room.setId(101L); room.setRoomType("Suite"); room.setPricePerNight(BigDecimal.valueOf(200)); room.setHotel(hotel);

        booking = new Booking(); booking.setId(1L); booking.setRoom(room); booking.setUser(user);
        booking.setCheckInDate(checkIn); booking.setCheckOutDate(checkOut); booking.setStatus("CONFIRMED");
    }

    @Test
    void shouldCreateBookingWhenRoomIsAvailable() {
        // Arrange
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(eq(room.getId()), eq(checkIn), eq(checkOut)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        
        // Act
        BookingDto createdBookingDto = bookingService.createBooking(room.getId(), user, checkIn, checkOut);

        // Assert
        verify(bookingRepository, times(1)).save(any(Booking.class));
        assertThat(createdBookingDto.getRoomId()).isEqualTo(room.getId());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenRoomNotAvailable() {
        // Arrange
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(eq(room.getId()), eq(checkIn), eq(checkOut)))
                .thenReturn(Collections.singletonList(new Booking())); // Room is booked

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(room.getId(), user, checkIn, checkOut);
        });
        assertThat(exception.getMessage()).contains("room is not available");
        verify(bookingRepository, never()).save(any(Booking.class));
    }

     @Test
    void shouldThrowIllegalArgumentExceptionWhenRoomNotFound() {
        // Arrange
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(99L, user, checkIn, checkOut);
        });
         assertThat(exception.getMessage()).contains("Invalid Room ID");
        verify(bookingRepository, never()).save(any(Booking.class));
    }

     @Test
    void shouldThrowIllegalArgumentExceptionForInvalidDates() {
        // Arrange
         LocalDate pastDate = LocalDate.now().minusDays(1L);
        
         // Act & Assert
         IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
             bookingService.createBooking(room.getId(), user, pastDate, checkOut);
         });
         assertThat(ex.getMessage()).contains("Invalid booking dates");

         // Verify repository was never called because date validation failed first
         verify(roomRepository, never()).findById(anyLong());
         verify(bookingRepository, never()).save(any(Booking.class));
    }
    
    @Test
    void shouldCancelBookingSuccessfully() {
        // Arrange
        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        // Act
        bookingService.cancelBooking(1L, user);

        // Assert
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo("CANCELLED");
    }
}