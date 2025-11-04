package com.booking.hotelbookingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchResultDto {
    private Long roomId;
    private String roomType;
    private int capacity;
    private BigDecimal pricePerNight;
}