package com.booking.hotelbookingsystem.repository;

import com.booking.hotelbookingsystem.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindByEmailWhenEmailExists() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("findme@example.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        // Act
        User foundUser = userRepository.findByEmail("findme@example.com");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("findme@example.com");
    }

    @Test
    void shouldReturnNullWhenEmailDoesNotExist() {
        // Act
        User foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isNull();
    }
}