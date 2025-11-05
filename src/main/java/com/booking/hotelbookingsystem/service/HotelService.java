package com.booking.hotelbookingsystem.service;

import com.booking.hotelbookingsystem.dto.HotelDto;
import com.booking.hotelbookingsystem.dto.HotelSearchResultDto;
import com.booking.hotelbookingsystem.model.Hotel;
import java.time.LocalDate;
import java.util.List;

public interface HotelService {
    
    // Admin Methods
    void saveHotel(HotelDto hotelDto);
    List<HotelDto> findAllHotels();
    Hotel findHotelById(Long id);
    HotelDto findHotelDtoById(Long id);
    void updateHotel(HotelDto hotelDto);
    void deleteHotel(Long id);

    // Search Method
    List<HotelSearchResultDto> searchAvailableHotels(String city, LocalDate checkInDate, LocalDate checkOutDate);

    // --- ADD THIS METHOD ---
    long countHotels();
}