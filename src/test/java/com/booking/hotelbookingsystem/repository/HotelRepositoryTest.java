package com.booking.hotelbookingsystem.repository;

import com.booking.hotelbookingsystem.model.Hotel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @BeforeEach
    void setUp() {
        Hotel hotel1 = new Hotel();
        hotel1.setName("Hotel Alpha");
        hotel1.setCity("Paris");
        hotel1.setAddress("1 Rue Alpha");
        hotelRepository.save(hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setName("Hotel Beta");
        hotel2.setCity("paris"); // Lowercase city
        hotel2.setAddress("2 Rue Beta");
        hotelRepository.save(hotel2);

         Hotel hotel3 = new Hotel();
         hotel3.setName("Hotel Gamma");
         hotel3.setCity("London");
         hotel3.setAddress("3 Downing St");
         hotelRepository.save(hotel3);
    }

     @AfterEach
    void tearDown() {
        hotelRepository.deleteAll();
    }

    @Test
    void shouldFindByCityIgnoreCase() {
        // Act
        List<Hotel> foundHotels = hotelRepository.findByCityIgnoreCase("Paris");

        // Assert
        assertThat(foundHotels).hasSize(2);
        assertThat(foundHotels).extracting(Hotel::getName).containsExactlyInAnyOrder("Hotel Alpha", "Hotel Beta");
    }

    @Test
    void shouldReturnEmptyListWhenCityNotFound() {
         // Act
        List<Hotel> foundHotels = hotelRepository.findByCityIgnoreCase("Tokyo");

        // Assert
        assertThat(foundHotels).isEmpty();
    }
}