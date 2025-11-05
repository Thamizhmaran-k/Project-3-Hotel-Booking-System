package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.RoomDto;
import com.booking.hotelbookingsystem.model.Hotel;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.repository.HotelRepository;
import com.booking.hotelbookingsystem.repository.RoomRepository;
import com.booking.hotelbookingsystem.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomServiceImpl(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    @Override
    @Transactional
    public void saveRoom(RoomDto roomDto) {
        Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Hotel ID:" + roomDto.getHotelId()));
        Room room = new Room();
        mapDtoToEntity(roomDto, room, hotel);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> findRoomsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + hotelId));
        return hotel.getRooms().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoomDto findRoomDtoById(Long roomId) {
         Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + roomId));
         return mapEntityToDto(room);
    }

    @Override
    @Transactional
    public void updateRoom(RoomDto roomDto) {
        Room room = roomRepository.findById(roomDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + roomDto.getId()));
        mapDtoToEntity(roomDto, room, null);
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new EntityNotFoundException("Room not found with ID: " + roomId);
        }
        roomRepository.deleteById(roomId);
    }

    // --- ADD THIS METHOD ---
    @Override
    public long countRooms() {
        return roomRepository.count();
    }
    
    // --- Helper Methods ---
    private RoomDto mapEntityToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomType(room.getRoomType());
        dto.setCapacity(room.getCapacity());
        dto.setPricePerNight(room.getPricePerNight());
        if (room.getHotel() != null) {
            dto.setHotelId(room.getHotel().getId());
        }
        return dto;
    }

    private void mapDtoToEntity(RoomDto dto, Room entity, Hotel hotel) {
        entity.setRoomType(dto.getRoomType());
        entity.setCapacity(dto.getCapacity());
        entity.setPricePerNight(dto.getPricePerNight());
        if (hotel != null) {
            entity.setHotel(hotel);
        }
    }
}