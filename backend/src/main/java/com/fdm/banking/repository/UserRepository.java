package com.fdm.banking.repository;

import com.fdm.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    /** Used by JwtAuthenticationFilter to resolve the JWT sub claim (Group 1 UUID). */
    Optional<User> findByExternalSubjectId(String externalSubjectId);
}
