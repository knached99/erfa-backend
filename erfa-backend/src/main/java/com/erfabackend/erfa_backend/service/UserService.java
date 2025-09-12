package com.erfabackend.erfa_backend.service;

import com.erfabackend.erfa_backend.model.User;
import com.erfabackend.erfa_backend.model.VerificationToken;
import com.erfabackend.erfa_backend.repository.UserRepository;
import com.erfabackend.erfa_backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository tokenRepository;

    public User registerUser(User user) {
        user.setEnabled(false);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public VerificationToken createVerificationToken(User user, String token, String expiryDate) {
        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .build();
        return tokenRepository.save(verificationToken);
    }

    public Optional<VerificationToken> getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }
}
