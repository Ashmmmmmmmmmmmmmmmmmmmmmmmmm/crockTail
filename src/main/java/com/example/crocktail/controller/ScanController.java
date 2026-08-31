package com.example.crocktail.controller;

import com.example.crocktail.model.*;
import com.example.crocktail.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScanController {

    private final ScanService scanService;
    private final BatchScanService batchScanService;
    private final QueryLogService queryLogService;

    public ScanController(ScanService scanService,
                          BatchScanService batchScanService,
                          QueryLogService queryLogService) {
        this.scanService = scanService;
        this.batchScanService = batchScanService;
        this.queryLogService = queryLogService;
    }

    @GetMapping("/scan")
    public ScanResult scan(@RequestParam String domain) {
        return scanService.scan(domain);
    }

    @PostMapping("/scan/batch")
    public BatchScanResult batchScan(@RequestBody BatchScanRequest request) {
        return batchScanService.batchScan(request.getDomains());
    }

    @PostMapping("/scan/log")
    public String saveToLog(@RequestBody ScanResult result) {
        queryLogService.logQuery(result);
        return "Logged successfully";
    }
}