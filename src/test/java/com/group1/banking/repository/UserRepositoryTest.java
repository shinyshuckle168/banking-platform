package com.group1.banking.repository;

import com.group1.banking.entity.User;
import com.group1.banking.enums.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user and find by username ignore case")
    void shouldFindByUsernameIgnoreCase() {
        User user = new User();
        user.setUsername("tarun@example.com");
        user.setPasswordHash("hashed-password");
        user.setRoles(List.of(RoleName.CUSTOMER));
        user.setActive(true);

        userRepository.save(user);

        Optional<User> found = userRepository.findByUsernameIgnoreCase("TARUN@EXAMPLE.COM");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("tarun@example.com");
    }

    @Test
    @DisplayName("Should check existence by username ignore case")
    void shouldCheckExistsByUsernameIgnoreCase() {
        User user = new User();
        user.setUsername("tarun@example.com");
        user.setPasswordHash("hashed-password");
        user.setRoles(List.of(RoleName.CUSTOMER));
        user.setActive(true);

        userRepository.save(user);

        boolean exists = userRepository.existsByUsernameIgnoreCase("TARUN@EXAMPLE.COM");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return empty when user not found")
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> user = userRepository.findByUsernameIgnoreCase("notfound@example.com");

        assertThat(user).isEmpty();
    }
}