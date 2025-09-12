package com.erfabackend.erfa_backend.service;

import com.erfabackend.erfa_backend.model.ArtistData;
import com.erfabackend.erfa_backend.model.Artist;
import com.erfabackend.erfa_backend.repository.ArtistDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArtistDataService {
    @Autowired
    private ArtistDataRepository artistDataRepository;

    public List<ArtistData> getAllData() {
        return artistDataRepository.findAll();
    }

    public ArtistData saveData(ArtistData data) {
        return artistDataRepository.save(data);
    }
}
