package com.booking.hotelbookingsystem.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BookingDto {
    private Long id; // Booking ID

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    private Long userId;
    private String userName; // For display

    private Long roomId;
    private String roomType; // For display
    private BigDecimal pricePerNight; // For display

    private Long hotelId; // For display
    private String hotelName; // For display

    private String status;
}