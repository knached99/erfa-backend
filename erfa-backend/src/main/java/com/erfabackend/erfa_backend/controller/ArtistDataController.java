package com.erfabackend.erfa_backend.controller;

import com.erfabackend.erfa_backend.model.ArtistData;
import com.erfabackend.erfa_backend.service.ArtistDataService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class ArtistDataController {
    @Autowired
    private ArtistDataService artistDataService;

    @GetMapping
    public ResponseEntity<List<ArtistData>> getAllData() {
        return ResponseEntity.ok(artistDataService.getAllData());
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            String backupDir = "backups";
            String backupName = "artistdata_backup_" + java.time.LocalDate.now() + ".csv";
            com.erfabackend.erfa_backend.util.CsvUtils.backupCsv(file, backupDir, backupName);
            List<String> rows = com.erfabackend.erfa_backend.util.CsvUtils.parseCsv(file);
            for (String row : rows) {
                ArtistData data = ArtistData.builder()
                        .csvRowData(row)
                        .importDate(java.time.LocalDate.now().toString())
                        .build();
                artistDataService.saveData(data);
            }
            return ResponseEntity.ok("CSV uploaded and backed up");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
