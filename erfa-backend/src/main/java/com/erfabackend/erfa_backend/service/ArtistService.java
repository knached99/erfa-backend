package com.erfabackend.erfa_backend.service;

import com.erfabackend.erfa_backend.model.Artist;
import com.erfabackend.erfa_backend.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArtistService {
    @Autowired
    private ArtistRepository artistRepository;

    public List<Artist> searchArtists(String name) {
        return artistRepository.findByNameContainingIgnoreCase(name);
    }

    public Artist saveArtist(Artist artist) {
        return artistRepository.save(artist);
    }
}
