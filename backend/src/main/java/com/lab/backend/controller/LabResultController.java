package com.lab.backend.controller;

import com.lab.backend.dto.LabResultAnalysisDto;
import com.lab.backend.dto.LabResultDto;
import com.lab.backend.service.LabResultService;
import com.lab.backend.service.OllamaService;
import com.lab.backend.model.ResultStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @GetMapping("/search")
    public ResponseEntity<List<LabResultDto>> searchByPatientId(@RequestParam String patientId) {
        log.info("Searching results for patientId={}", patientId);
        return ResponseEntity.ok(labResultService.searchByPatientId(patientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LabResultDto>> getByStatus(@PathVariable ResultStatus status) {
        log.info("Fetching results by status={}", status);
        return ResponseEntity.ok(labResultService.getByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabResultDto> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(labResultService.getResultById(id));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeResult(@PathVariable Long id) {
        log.info("LLM analysis requested for result id={}", id);
        LabResultDto result = labResultService.getResultById(id);

        if (result.getLlmAnalysis() != null && !result.getLlmAnalysis().isEmpty()) {
            log.info("Returning cached analysis for result id={}", id);
            return ResponseEntity.ok(result.getLlmAnalysis());
        }

        String analysis = ollamaService.analyze(result);
        labResultService.saveAnalysis(id, analysis);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/{id}/reanalyze")
    public ResponseEntity<String> reanalyzeResult(@PathVariable Long id) {
        log.info("LLM re-analysis requested for result id={}", id);
        LabResultDto result = labResultService.getResultById(id);
        String analysis = ollamaService.analyze(result);
        labResultService.saveAnalysis(id, analysis);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<LabResultDto>> getByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        log.info("Fetching results between {} and {}", startDate, endDate);
        return ResponseEntity.ok(labResultService.getByDateRange(startDate, endDate));
    }

    @GetMapping("/{id}/analyses")
    public ResponseEntity<List<LabResultAnalysisDto>> getAnalysisHistory(@PathVariable Long id) {
        log.info("Fetching analysis history for result id={}", id);
        return ResponseEntity.ok(labResultService.getAnalysisHistory(id));
    }

}