package com.erfabackend.erfa_backend.controller;

import com.erfabackend.erfa_backend.util.JwtService;
import com.erfabackend.erfa_backend.model.User;
import com.erfabackend.erfa_backend.model.VerificationToken;
import com.erfabackend.erfa_backend.service.UserService;
import com.erfabackend.erfa_backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private com.erfabackend.erfa_backend.service.RefreshTokenService refreshTokenService;
    @org.springframework.beans.factory.annotation.Value("${app.backend.base-url:http://localhost:8081}")
    private String backendBaseUrl;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        // Basic validation to avoid 500s
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        // Prevent duplicate email 500s
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("Email already registered");
        }

        // Hash password and save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userService.registerUser(user);

        // Generate verification token
        String token = java.util.UUID.randomUUID().toString();
        String expiryDate = java.time.LocalDate.now().plusDays(1).toString();
        userService.createVerificationToken(savedUser, token, expiryDate);

        // Send verification email (don't fail signup if SMTP isn't configured)
    String link = backendBaseUrl + "/api/auth/verify?token=" + token;
        try {
            emailService.sendEmail(savedUser.getEmail(), "Verify your account", "Click the link to verify: " + link);
        } catch (Exception ex) {
            log.warn("Signup succeeded but failed to send verification email: {}", ex.toString());
        }
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Signup successful. If email is configured, a verification email was sent.",
                "verificationToken", token
        ));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody java.util.Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        java.util.Optional<User> u = userService.findByEmail(email);
        if (u.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User user = u.get();
        if (user.isEnabled()) {
            return ResponseEntity.ok("Account already verified");
        }
        String token = java.util.UUID.randomUUID().toString();
        String expiryDate = java.time.LocalDate.now().plusDays(1).toString();
        userService.createVerificationToken(user, token, expiryDate);
        String link = backendBaseUrl + "/api/auth/verify?token=" + token;
        try {
            emailService.sendEmail(user.getEmail(), "Verify your account", "Click the link to verify: " + link);
        } catch (Exception ex) {
            log.warn("Resend verification failed to send email: {}", ex.toString());
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Verification email sent (if SMTP configured)", "verificationToken", token));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        java.util.Optional<VerificationToken> vToken = userService.getVerificationToken(token);
        if (vToken.isPresent()) {
            User user = vToken.get().getUser();
            userService.enableUser(user);
            return ResponseEntity.ok("Verified and account activated!");
        } else {
            return ResponseEntity.status(400).body("Invalid or expired token");
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        java.util.Optional<User> userOpt = userService.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body("Account not verified");
            }
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtService.generateToken(user.getEmail());
                com.erfabackend.erfa_backend.model.RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
                return ResponseEntity.ok(java.util.Map.of("token", token, "refreshToken", refreshToken.getToken()));
            } else {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    // Developer/test endpoint to send arbitrary email (POST /api/auth/test-email)
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody java.util.Map<String, String> payload) {
        String to = payload.getOrDefault("to", null);
        String subject = payload.getOrDefault("subject", "Test Email");
        String body = payload.getOrDefault("body", "This is a test email from erfa-backend.");
        if (to == null) {
            return ResponseEntity.badRequest().body("Missing 'to' in payload");
        }
        emailService.sendEmail(to, subject, body);
        return ResponseEntity.ok("Email sent (if SMTP configured)");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody java.util.Map<String, String> payload) {
        String refresh = payload.get("refreshToken");
        if (refresh == null) return ResponseEntity.badRequest().body("Missing refreshToken");

        java.util.Optional<com.erfabackend.erfa_backend.model.RefreshToken> rtOpt = refreshTokenService.findByToken(refresh);
        if (rtOpt.isEmpty()) return ResponseEntity.status(401).body("Invalid refresh token");
        com.erfabackend.erfa_backend.model.RefreshToken rt = rtOpt.get();
        if (refreshTokenService.isExpired(rt)) {
            return ResponseEntity.status(401).body("Refresh token expired or revoked");
        }
        String newToken = jwtService.generateToken(rt.getUser().getEmail());
        // rotate: revoke old and issue new
        refreshTokenService.revokeToken(rt);
        com.erfabackend.erfa_backend.model.RefreshToken newRt = refreshTokenService.createRefreshToken(rt.getUser());
        return ResponseEntity.ok(java.util.Map.of("token", newToken, "refreshToken", newRt.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody java.util.Map<String, String> payload) {
        String refresh = payload.get("refreshToken");
        if (refresh == null) return ResponseEntity.badRequest().body("Missing refreshToken");
        java.util.Optional<com.erfabackend.erfa_backend.model.RefreshToken> rtOpt = refreshTokenService.findByToken(refresh);
        if (rtOpt.isEmpty()) return ResponseEntity.ok("Token not found, treated as logged out");
        refreshTokenService.revokeToken(rtOpt.get());
        return ResponseEntity.ok("Logged out");
    }

    // Authenticated endpoint: returns current user info from JWT
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            username = ud.getUsername();
        } else {
            username = String.valueOf(principal);
        }
        java.util.Set<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(java.util.Map.of(
                "email", username,
                "roles", roles
        ));
    }
}
