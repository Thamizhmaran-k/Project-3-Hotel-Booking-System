package com.booking.hotelbookingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomType; // e.g., "Single", "Double", "Suite"

    @Column(nullable = false)
    private int capacity; // e.g., 2 (for 2 people)

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    // Many rooms can belong to one hotel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}