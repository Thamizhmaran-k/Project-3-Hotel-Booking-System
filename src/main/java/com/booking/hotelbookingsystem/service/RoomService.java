package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.dto.RoomDto;
import java.util.List;

public interface RoomService {
    void saveRoom(RoomDto roomDto);
    List<RoomDto> findRoomsByHotelId(Long hotelId);
    RoomDto findRoomDtoById(Long roomId);
    void updateRoom(RoomDto roomDto);
    void deleteRoom(Long roomId);
    
    // --- ADD THIS METHOD ---
    long countRooms();
}