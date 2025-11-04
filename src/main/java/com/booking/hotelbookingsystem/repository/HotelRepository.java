package com.booking.hotelbookingsystem.repository;

import com.booking.hotelbookingsystem.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    // This method will be used for searching later
    List<Hotel> findByCityIgnoreCase(String city);
}