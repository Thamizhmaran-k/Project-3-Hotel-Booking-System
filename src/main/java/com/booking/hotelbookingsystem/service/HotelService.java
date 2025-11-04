package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.dto.HotelDto;
import com.booking.hotelbookingsystem.dto.HotelSearchResultDto; // Import
import com.booking.hotelbookingsystem.model.Hotel;
import java.time.LocalDate; // Import
import java.util.List;

public interface HotelService {
    
    // --- Admin Methods ---
    void saveHotel(HotelDto hotelDto);
    List<HotelDto> findAllHotels();
    Hotel findHotelById(Long id);
    HotelDto findHotelDtoById(Long id);
    void updateHotel(HotelDto hotelDto);
    void deleteHotel(Long id);

    // --- NEW User Search Method ---
    List<HotelSearchResultDto> searchAvailableHotels(String city, LocalDate checkInDate, LocalDate checkOutDate);
}