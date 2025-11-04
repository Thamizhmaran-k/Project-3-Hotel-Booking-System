package com.booking.hotelbookingsystem.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HotelSearchResultDto {
    private Long hotelId;
    private String hotelName;
    private String city;
    private String address;
    private String description;
    private List<RoomSearchResultDto> availableRooms = new ArrayList<>();
}