package com.booking.hotelbookingsystem.service.impl;

import com.booking.hotelbookingsystem.dto.HotelDto;
import com.booking.hotelbookingsystem.dto.HotelSearchResultDto;
import com.booking.hotelbookingsystem.dto.RoomSearchResultDto;
import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Hotel;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.repository.BookingRepository; // Import
import com.booking.hotelbookingsystem.repository.HotelRepository;
import com.booking.hotelbookingsystem.service.HotelService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // Import
import java.util.ArrayList; // Import
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository; // Inject BookingRepository

    // Update constructor
    public HotelServiceImpl(HotelRepository hotelRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void saveHotel(HotelDto hotelDto) {
        Hotel hotel = new Hotel();
        mapDtoToEntity(hotelDto, hotel);
        hotelRepository.save(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDto> findAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Hotel findHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDto findHotelDtoById(Long id) {
        return hotelRepository.findById(id)
                .map(this::mapEntityToDto)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + id));
    }

    @Override
    @Transactional
    public void updateHotel(HotelDto hotelDto) {
        Hotel hotel = hotelRepository.findById(hotelDto.getId())
                 .orElseThrow(() -> new EntityNotFoundException("Hotel not found with ID: " + hotelDto.getId()));
        
        mapDtoToEntity(hotelDto, hotel);
        hotelRepository.save(hotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
             throw new EntityNotFoundException("Hotel not found with ID: " + id);
        }
        hotelRepository.deleteById(id);
    }
    

    // --- NEW SEARCH IMPLEMENTATION ---
    @Override
    @Transactional(readOnly = true)
    public List<HotelSearchResultDto> searchAvailableHotels(String city, LocalDate checkInDate, LocalDate checkOutDate) {
        
        // 1. Find hotels in the given city
        List<Hotel> hotelsInCity = hotelRepository.findByCityIgnoreCase(city);
        List<HotelSearchResultDto> results = new ArrayList<>();

        // 2. For each hotel, check its rooms for availability
        for (Hotel hotel : hotelsInCity) {
            HotelSearchResultDto hotelResultDto = mapEntityToSearchDto(hotel);
            
            // 3. Check availability for each room in this hotel
            List<RoomSearchResultDto> availableRooms = new ArrayList<>();
            for (Room room : hotel.getRooms()) {
                if (isRoomAvailable(room.getId(), checkInDate, checkOutDate)) {
                    availableRooms.add(mapRoomToSearchDto(room));
                }
            }

            // 4. Only add the hotel to results if it has available rooms
            if (!availableRooms.isEmpty()) {
                hotelResultDto.setAvailableRooms(availableRooms);
                results.add(hotelResultDto);
            }
        }
        return results;
    }

    // --- Helper Methods ---

    // Check if a specific room is available (no overlaps)
    private boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(roomId, checkInDate, checkOutDate);
        return overlappingBookings.isEmpty();
    }

    private void mapDtoToEntity(HotelDto dto, Hotel entity) {
        entity.setName(dto.getName());
        entity.setCity(dto.getCity());
        entity.setAddress(dto.getAddress());
        entity.setDescription(dto.getDescription());
    }

    private HotelDto mapEntityToDto(Hotel entity) {
        HotelDto dto = new HotelDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCity(entity.getCity());
        dto.setAddress(entity.getAddress());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    // Helper to map Hotel to its search result DTO
    private HotelSearchResultDto mapEntityToSearchDto(Hotel hotel) {
        HotelSearchResultDto dto = new HotelSearchResultDto();
        dto.setHotelId(hotel.getId());
        dto.setHotelName(hotel.getName());
        dto.setCity(hotel.getCity());
        dto.setAddress(hotel.getAddress());
        dto.setDescription(hotel.getDescription());
        return dto;
    }

    // Helper to map Room to its search result DTO
    private RoomSearchResultDto mapRoomToSearchDto(Room room) {
        return new RoomSearchResultDto(
                room.getId(),
                room.getRoomType(),
                room.getCapacity(),
                room.getPricePerNight()
        );
    }
}