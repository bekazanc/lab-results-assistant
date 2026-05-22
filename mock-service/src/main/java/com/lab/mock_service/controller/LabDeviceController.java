package com.lab.mock_service.controller;

import com.lab.mock_service.service.LabResultGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/lab-results")
@RequiredArgsConstructor
public class LabDeviceController {

    private final LabResultGenerator generator;

    @GetMapping
    public ResponseEntity<Object> getLatestResult() {
        Object result = generator.generate();
        log.info("Mock device called: {}", result);
        return ResponseEntity.ok(result);
    }
}