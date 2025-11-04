package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.RoomDto;
import com.booking.hotelbookingsystem.model.Hotel;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.repository.HotelRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {

    @Mock private RoomRepository roomRepository;
    @Mock private HotelRepository hotelRepository;

    @InjectMocks private RoomServiceImpl roomService;

    private Hotel hotel;
    private Room room;
    private RoomDto roomDto;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Hotel For Rooms");

        room = new Room();
        room.setId(101L);
        room.setRoomType("Deluxe");
        room.setHotel(hotel);

        roomDto = new RoomDto();
        roomDto.setRoomType("Standard");
        roomDto.setCapacity(1);
        roomDto.setPricePerNight(BigDecimal.valueOf(100));
        roomDto.setHotelId(hotel.getId());
    }

    @Test
    void shouldSaveRoomSuccessfully() {
        // Arrange
        when(hotelRepository.findById(roomDto.getHotelId())).thenReturn(Optional.of(hotel));
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);

        // Act
        roomService.saveRoom(roomDto);

        // Assert
        verify(roomRepository, times(1)).save(roomCaptor.capture());
        Room savedRoom = roomCaptor.getValue();
        assertThat(savedRoom.getRoomType()).isEqualTo("Standard");
        assertThat(savedRoom.getHotel()).isEqualTo(hotel);
    }
    
    @Test
    void shouldThrowExceptionWhenSaveRoomWithInvalidHotelId() {
        // Arrange
        when(hotelRepository.findById(anyLong())).thenReturn(Optional.empty());
        roomDto.setHotelId(99L); // Invalid ID

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            roomService.saveRoom(roomDto);
        });
        verify(roomRepository, never()).save(any(Room.class));
    }

     @Test
    void shouldFindRoomsByHotelId() {
        // Arrange
        Room room2 = new Room(); room2.setId(102L); room2.setHotel(hotel); room2.setRoomType("Suite");
        hotel.setRooms(new ArrayList<>(List.of(room, room2)));
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));

        // Act
        List<RoomDto> foundRooms = roomService.findRoomsByHotelId(hotel.getId());

        // Assert
        assertThat(foundRooms).hasSize(2);
        assertThat(foundRooms).extracting(RoomDto::getRoomType).containsExactlyInAnyOrder("Deluxe", "Suite");
    }
}