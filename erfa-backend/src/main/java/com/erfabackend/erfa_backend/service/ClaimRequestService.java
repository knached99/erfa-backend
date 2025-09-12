package com.erfabackend.erfa_backend.service;

import com.erfabackend.erfa_backend.model.ClaimRequest;
import com.erfabackend.erfa_backend.repository.ClaimRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClaimRequestService {
    @Autowired
    private ClaimRequestRepository claimRequestRepository;

    public ClaimRequest saveRequest(ClaimRequest request) {
        return claimRequestRepository.save(request);
    }

    public List<ClaimRequest> getRequestsByStatus(String status) {
        return claimRequestRepository.findByStatus(status);
    }

    public List<ClaimRequest> getAllRequests() {
        return claimRequestRepository.findAll();
    }
}
