package com.booking.hotelbookingsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotEmpty
    private String name;

    @Column(nullable = false)
    @NotEmpty
    private String city;

    @Column(nullable = false)
    @NotEmpty
    private String address;

    @Column(length = 1000)
    private String description;

    // A hotel can have many rooms
    // mappedBy="hotel": The 'hotel' field in the Room class owns this relationship.
    // CascadeType.ALL: If a hotel is deleted, all its rooms are also deleted.
    // orphanRemoval=true: If you remove a room from this list, it gets deleted from the DB.
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();
}