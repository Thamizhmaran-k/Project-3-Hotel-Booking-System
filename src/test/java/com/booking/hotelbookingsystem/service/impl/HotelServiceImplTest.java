package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.HotelDto;
import com.booking.hotelbookingsystem.dto.HotelSearchResultDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Hotel;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.repository.BookingRepository;
import com.booking.hotelbookingsystem.repository.HotelRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceImplTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelDto hotelDto;
    private Room room1, room2;
    private LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Test Hotel");
        hotel.setCity("Testville");
        hotel.setAddress("123 Main St");
        hotel.setDescription("A nice hotel");

        room1 = new Room(); room1.setId(101L); room1.setRoomType("Single"); room1.setHotel(hotel); room1.setPricePerNight(BigDecimal.valueOf(100));
        room2 = new Room(); room2.setId(102L); room2.setRoomType("Double"); room2.setHotel(hotel); room2.setPricePerNight(BigDecimal.valueOf(150));
        hotel.setRooms(new ArrayList<>(List.of(room1, room2)));

        hotelDto = new HotelDto();
        hotelDto.setId(1L);
        hotelDto.setName("Updated Hotel");
        hotelDto.setCity("New City");
        hotelDto.setAddress("456 Updated St");
        hotelDto.setDescription("Updated description");
    }

    @Test
    void shouldFindHotelDtoById() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        HotelDto foundDto = hotelService.findHotelDtoById(1L);
        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getName()).isEqualTo(hotel.getName());
    }

    @Test
    void shouldThrowExceptionWhenFindHotelDtoByIdNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> hotelService.findHotelDtoById(99L));
    }

    @Test
    void shouldUpdateHotel() {
         when(hotelRepository.findById(hotelDto.getId())).thenReturn(Optional.of(hotel));
         ArgumentCaptor<Hotel> hotelCaptor = ArgumentCaptor.forClass(Hotel.class);

         hotelService.updateHotel(hotelDto);

         verify(hotelRepository).save(hotelCaptor.capture());
         Hotel updatedHotel = hotelCaptor.getValue();
         assertThat(updatedHotel.getName()).isEqualTo(hotelDto.getName());
    }

    @Test
    void shouldDeleteHotel() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        doNothing().when(hotelRepository).deleteById(1L);
        hotelService.deleteHotel(1L);
        verify(hotelRepository, times(1)).deleteById(1L);
    }

     @Test
    void shouldSearchAvailableHotelsAndFindAvailableRooms() {
        // Arrange
        LocalDate checkIn = today.plusDays(1);
        LocalDate checkOut = today.plusDays(5);
        when(hotelRepository.findByCityIgnoreCase("Testville")).thenReturn(List.of(hotel));
        
        // Room 1 is booked
        when(bookingRepository.findOverlappingBookings(room1.getId(), checkIn, checkOut))
                .thenReturn(List.of(new Booking()));
        // Room 2 is available
        when(bookingRepository.findOverlappingBookings(room2.getId(), checkIn, checkOut))
                .thenReturn(Collections.emptyList());

        // Act
        List<HotelSearchResultDto> results = hotelService.searchAvailableHotels("Testville", checkIn, checkOut);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAvailableRooms()).hasSize(1);
        assertThat(results.get(0).getAvailableRooms().get(0).getRoomId()).isEqualTo(room2.getId());
    }
    
    @Test
    void shouldReturnNoHotelsIfNoRoomsAreAvailable() {
        // Arrange
        LocalDate checkIn = today.plusDays(1);
        LocalDate checkOut = today.plusDays(5);
        when(hotelRepository.findByCityIgnoreCase("Testville")).thenReturn(List.of(hotel));

        // Both rooms are booked
        when(bookingRepository.findOverlappingBookings(anyLong(), eq(checkIn), eq(checkOut)))
                .thenReturn(List.of(new Booking()));

        // Act
        List<HotelSearchResultDto> results = hotelService.searchAvailableHotels("Testville", checkIn, checkOut);

        // Assert
        assertThat(results).isEmpty();
    }
}