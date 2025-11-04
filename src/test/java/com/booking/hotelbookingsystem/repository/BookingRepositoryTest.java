package com.booking.hotelbookingsystem.repository;

import com.booking.hotelbookingsystem.model.Booking;
import com.booking.hotelbookingsystem.model.Hotel;
import com.booking.hotelbookingsystem.model.Room;
import com.booking.hotelbookingsystem.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private HotelRepository hotelRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private RoleRepository roleRepository; 

    private User user1, user2;
    private Room room1, room2;
    private LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        // Create Users
        user1 = new User(); user1.setName("User One"); user1.setEmail("user1@test.com"); user1.setPassword("pwd");
        user2 = new User(); user2.setName("User Two"); user2.setEmail("user2@test.com"); user2.setPassword("pwd");
        userRepository.saveAll(List.of(user1, user2));

        // Create Hotel and Rooms
        Hotel hotel = new Hotel(); hotel.setName("Test Hotel"); hotel.setCity("Test City"); hotel.setAddress("Addr");
        hotelRepository.save(hotel);
        room1 = new Room(); room1.setRoomType("Single"); room1.setCapacity(1); room1.setPricePerNight(BigDecimal.TEN); room1.setHotel(hotel);
        room2 = new Room(); room2.setRoomType("Double"); room2.setCapacity(2); room2.setPricePerNight(BigDecimal.valueOf(20)); room2.setHotel(hotel);
        roomRepository.saveAll(List.of(room1, room2));

        // Create Bookings
        Booking booking1 = new Booking(); booking1.setUser(user1); booking1.setRoom(room1);
        booking1.setCheckInDate(today.plusDays(1)); booking1.setCheckOutDate(today.plusDays(5)); booking1.setStatus("CONFIRMED");

        Booking booking2 = new Booking(); booking2.setUser(user2); booking2.setRoom(room1); // Same room, different dates
        booking2.setCheckInDate(today.plusDays(10)); booking2.setCheckOutDate(today.plusDays(15)); booking2.setStatus("CONFIRMED");

        Booking booking3 = new Booking(); booking3.setUser(user1); booking3.setRoom(room2); // Different room
        booking3.setCheckInDate(today.plusDays(2)); booking3.setCheckOutDate(today.plusDays(4)); booking3.setStatus("CONFIRMED");

         Booking booking4 = new Booking(); booking4.setUser(user1); booking4.setRoom(room1); // Cancelled booking
         booking4.setCheckInDate(today.plusDays(3)); booking4.setCheckOutDate(today.plusDays(6)); booking4.setStatus("CANCELLED");

        bookingRepository.saveAll(List.of(booking1, booking2, booking3, booking4));
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void shouldFindOverlappingBookings() {
        LocalDate searchCheckIn = today.plusDays(3);
        LocalDate searchCheckOut = today.plusDays(7);
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(room1.getId(), searchCheckIn, searchCheckOut);
        assertThat(overlapping).hasSize(1);
    }

     @Test
    void shouldNotFindOverlappingBookingsWhenDatesDontOverlap() {
        LocalDate searchCheckIn = today;
        LocalDate searchCheckOut = today.plusDays(1);
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(room1.getId(), searchCheckIn, searchCheckOut);
        assertThat(overlapping).isEmpty();
    }
    
    @Test
    void shouldNotFindCancelledBookings() {
        LocalDate searchCheckIn = today.plusDays(4);
        LocalDate searchCheckOut = today.plusDays(5);
        // This overlaps with booking1 (CONFIRMED) and booking4 (CANCELLED)
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(room1.getId(), searchCheckIn, searchCheckOut);
        // Should only find the CONFIRMED one
        assertThat(overlapping).hasSize(1);
    }

    @Test
    void shouldFindBookingsByUserIdOrderedByIdDesc() {
        List<Booking> user1Bookings = bookingRepository.findByUserIdOrderByIdDesc(user1.getId());
        assertThat(user1Bookings).hasSize(3);
        assertThat(user1Bookings.get(0).getStatus()).isEqualTo("CANCELLED"); // booking4 (newest ID)
        assertThat(user1Bookings.get(1).getRoom().getRoomType()).isEqualTo("Double"); // booking3
        assertThat(user1Bookings.get(2).getRoom().getRoomType()).isEqualTo("Single"); // booking1
    }
}