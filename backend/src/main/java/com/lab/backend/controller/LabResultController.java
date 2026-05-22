package com.lab.backend.controller;

import com.lab.backend.dto.LabResultDto;
import com.lab.backend.service.LabResultService;
import com.lab.backend.service.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class LabResultController {

    private final LabResultService labResultService;
    private final OllamaService ollamaService;

    @GetMapping
    public ResponseEntity<List<LabResultDto>> getAllResults() {
        return ResponseEntity.ok(labResultService.getAllResults());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabResultDto> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(labResultService.getResultById(id));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeResult(@PathVariable Long id) {
        log.info("LLM analysis requested for result id={}", id);
        LabResultDto result = labResultService.getResultById(id);
        String analysis = ollamaService.analyze(result);
        return ResponseEntity.ok(analysis);
    }
}