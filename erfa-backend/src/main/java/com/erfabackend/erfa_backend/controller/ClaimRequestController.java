package com.erfabackend.erfa_backend.controller;

import com.erfabackend.erfa_backend.model.ClaimRequest;
import com.erfabackend.erfa_backend.model.User;
import com.erfabackend.erfa_backend.service.ClaimRequestService;
import com.erfabackend.erfa_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimRequestController {
    @Autowired
    private ClaimRequestService claimRequestService;
    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<ClaimRequest> submitClaim(@RequestBody ClaimRequest request) {
        ClaimRequest savedRequest = claimRequestService.saveRequest(request);
        // Send email to admin (replace with actual admin email)
        String adminEmail = "admin@erfa.com";
        emailService.sendEmail(adminEmail, "New Artist Claim Request", "A user has requested to claim artist: " + request.getArtist().getName());
        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping
    public ResponseEntity<List<ClaimRequest>> getAllClaims() {
        return ResponseEntity.ok(claimRequestService.getAllRequests());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveClaim(@PathVariable Long id) {
        ClaimRequest request = claimRequestService.getAllRequests().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if (request == null) return ResponseEntity.status(404).body("Request not found");
        request.setStatus("approved");
        claimRequestService.saveRequest(request);
        User user = request.getUser();
        emailService.sendEmail(user.getEmail(), "Artist Claim Approved", "Your request to claim artist " + request.getArtist().getName() + " has been approved.");
        return ResponseEntity.ok("Approved");
    }

    @PostMapping("/{id}/deny")
    public ResponseEntity<?> denyClaim(@PathVariable Long id, @RequestBody String notes) {
        ClaimRequest request = claimRequestService.getAllRequests().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if (request == null) return ResponseEntity.status(404).body("Request not found");
        request.setStatus("denied");
        request.setNotes(notes);
        claimRequestService.saveRequest(request);
        User user = request.getUser();
        emailService.sendEmail(user.getEmail(), "Artist Claim Denied", "Your request to claim artist " + request.getArtist().getName() + " was denied. Reason: " + notes);
        return ResponseEntity.ok("Denied");
    }
}
