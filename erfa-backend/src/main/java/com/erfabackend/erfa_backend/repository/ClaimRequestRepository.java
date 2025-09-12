package com.erfabackend.erfa_backend.repository;

import com.erfabackend.erfa_backend.model.ClaimRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Long> {
    List<ClaimRequest> findByStatus(String status);
}
