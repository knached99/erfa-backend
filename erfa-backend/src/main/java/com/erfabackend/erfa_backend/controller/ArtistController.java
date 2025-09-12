package com.erfabackend.erfa_backend.controller;

import com.erfabackend.erfa_backend.model.Artist;
import com.erfabackend.erfa_backend.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {
    @Autowired
    private ArtistService artistService;

    @GetMapping("/search")
    public ResponseEntity<List<Artist>> searchArtists(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "query", required = false) String query
    ) {
        String term = null;
        if (query != null && !query.isBlank()) {
            term = query;
        } else if (q != null && !q.isBlank()) {
            term = q;
        } else if (name != null && !name.isBlank()) {
            term = name;
        }
        if (term == null || term.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(artistService.searchArtists(term));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Artist>> searchArtistsPost(@RequestBody java.util.Map<String, String> payload) {
        String query = payload.get("query");
        String q = payload.get("q");
        String name = payload.get("name");
        String term = null;
        if (query != null && !query.isBlank()) {
            term = query;
        } else if (q != null && !q.isBlank()) {
            term = q;
        } else if (name != null && !name.isBlank()) {
            term = name;
        }
        if (term == null || term.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(artistService.searchArtists(term));
    }
}
