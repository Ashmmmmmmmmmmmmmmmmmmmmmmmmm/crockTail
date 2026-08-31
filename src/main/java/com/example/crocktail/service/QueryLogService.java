package com.example.crocktail.service;

import com.example.crocktail.model.ScanResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Service
public class QueryLogService {

    private static final String LOG_FILE = "query_log.txt";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logQuery(ScanResult result) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            String json = objectMapper.writeValueAsString(result);
            String logLine = "[" + LocalDateTime.now() + "] " + json;
            pw.println(logLine);

        } catch (IOException e) {
            System.err.println("Failed to write query log: " + e.getMessage());
        }
    }
}