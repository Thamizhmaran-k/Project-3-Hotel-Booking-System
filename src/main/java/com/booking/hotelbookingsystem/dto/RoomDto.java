package com.booking.hotelbookingsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RoomDto {
    private Long id;

    @NotEmpty(message = "Room type cannot be empty")
    private String roomType; // e.g., "Single", "Double", "Suite"

    @NotNull(message = "Capacity cannot be empty")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity; // How many people can stay

    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerNight;

    private Long hotelId; // To link back to the Hotel
}