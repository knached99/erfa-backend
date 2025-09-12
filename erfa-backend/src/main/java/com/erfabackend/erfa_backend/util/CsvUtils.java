package com.erfabackend.erfa_backend.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    public static List<String> parseCsv(MultipartFile file) throws IOException {
        List<String> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                rows.add(record.toString());
            }
        }
        return rows;
    }

    public static void backupCsv(MultipartFile file, String backupDir, String backupName) throws IOException {
        File dir = new File(backupDir);
        if (!dir.exists()) dir.mkdirs();
        File backupFile = new File(dir, backupName);
        try (OutputStream os = new FileOutputStream(backupFile)) {
            os.write(file.getBytes());
        }
    }
}
