package com.example.crocktail.service;

import com.example.crocktail.model.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BatchScanService {

    private final ScanService scanService;

    public BatchScanService(ScanService scanService ) {
        this.scanService = scanService;
    }

    public BatchScanResult batchScan(List<String> domains) {
        List<ScanResult> results = domains.stream()
                .map(domain -> scanService.scan(domain))
                .collect(Collectors.toList());

        BatchScanResult batchResult = new BatchScanResult();
        batchResult.setResults(results);
        return batchResult;
    }
}