package com.erfabackend.erfa_backend.repository;

import com.erfabackend.erfa_backend.model.RefreshToken;
import com.erfabackend.erfa_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(User user);
}
