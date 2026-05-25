package com.lab.backend.service;

import com.lab.backend.repository.LabResultAnalysisRepository;
import com.lab.backend.model.LabResultAnalysis;
import com.lab.backend.dto.LabResultAnalysisDto;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.lab.backend.dto.LabResultDto;
import com.lab.backend.dto.TestResultDto;
import com.lab.backend.model.LabResult;
import com.lab.backend.model.ResultStatus;
import com.lab.backend.model.TestResult;
import com.lab.backend.repository.LabResultRepository;
import com.lab.backend.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabResultService {

    private final LabResultRepository labResultRepository;
    private final TestResultRepository testResultRepository;
    private final LabResultAnalysisRepository labResultAnalysisRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${mock.service.url}")
    private String mockServiceUrl;

    @Scheduled(fixedDelay = 60000) // dakikada bir
    public void fetchFromMockService() {
        try {
            log.info("Fetching from mock service...");
            String response = restTemplate.getForObject(mockServiceUrl, String.class);
            processAndSave(response);
        } catch (Exception e) {
            log.error("Error fetching from mock service: {}", e.getMessage());
        }
    }

    public void processAndSave(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // missing required fields
            if (!root.hasNonNull("patientId") || !root.hasNonNull("deviceId")) {
                log.warn("INVALID: missing required fields. Skipping.");
                return;
            }

            // empty test list
            JsonNode testsNode = root.path("tests");
            if (!testsNode.isArray() || testsNode.isEmpty()) {
                log.warn("INVALID: empty test list for patientId={}. Skipping.",
                        root.path("patientId").asText());
                return;
            }

            String scenario = root.path("scenario").asText("UNKNOWN");
            List<TestResult> testResults = new ArrayList<>();
            ResultStatus status = ResultStatus.NORMAL;
            boolean hasDuplicate = false;
            java.util.Set<String> seenTests = new java.util.HashSet<>();

            for (JsonNode testNode : testsNode) {
                if (!testNode.hasNonNull("value") ||
                        !testNode.hasNonNull("referenceMin") ||
                        !testNode.hasNonNull("referenceMax")) {
                    log.warn("INVALID: missing fields in test. Rejecting entire result.");
                    return;
                }

                double value = testNode.path("value").asDouble();
                double min = testNode.path("referenceMin").asDouble();
                double max = testNode.path("referenceMax").asDouble();
                String testName = testNode.path("name").asText();

                // negative value
                if (value < 0) {
                    log.warn("INVALID: negative value {} for test {}. Rejecting entire result.",
                            value, testName);
                    return;
                }

                // duplicate test
                if (!seenTests.add(testName)) {
                    log.warn("INVALID: duplicate test {} detected. Skipping duplicate.", testName);
                    hasDuplicate = true;
                    continue;
                }

                boolean isAbnormal = value < min || value > max;
                if (isAbnormal) {
                    status = scenario.equals("CRITICAL") ? ResultStatus.CRITICAL : ResultStatus.ABNORMAL;
                }

                testResults.add(TestResult.builder()
                        .name(testName)
                        .value(value)
                        .unit(testNode.path("unit").asText())
                        .referenceMin(min)
                        .referenceMax(max)
                        .isAbnormal(isAbnormal)
                        .build());
            }

            if (testResults.isEmpty()) {
                log.warn("INVALID: no valid tests remaining for patientId={}. Skipping.",
                        root.path("patientId").asText());
                return;
            }

            if (hasDuplicate) {
                log.warn("WARNING: duplicate tests removed for patientId={}",
                        root.path("patientId").asText());
            }

            LabResult labResult = LabResult.builder()
                    .deviceId(root.path("deviceId").asText())
                    .patientId(root.path("patientId").asText())
                    .timestamp(LocalDateTime.now())
                    .scenario(scenario)
                    .rawJson(json)
                    .status(status)
                    .build();

            LabResult saved = labResultRepository.save(labResult);
            testResults.forEach(t -> t.setLabResult(saved));
            testResultRepository.saveAll(testResults);

            log.info("Saved lab result id={} status={}", saved.getId(), saved.getStatus());

        } catch (Exception e) {
            log.error("Error processing lab result: {}", e.getMessage());
        }
    }

    public List<LabResultDto> getAllResults() {
        return labResultRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public LabResultDto getResultById(Long id) {
        return labResultRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Result not found: " + id));
    }

    public List<LabResultDto> searchByPatientId(String patientId) {
        return labResultRepository.findByPatientIdContainingIgnoreCase(patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public void saveAnalysis(Long id, String analysis) {
        labResultRepository.findById(id).ifPresent(result -> {
            result.setLlmAnalysis(analysis);
            labResultRepository.save(result);

            LabResultAnalysis analysisRecord = LabResultAnalysis.builder()
                    .labResult(result)
                    .analysisText(analysis)
                    .build();
            labResultAnalysisRepository.save(analysisRecord);

            log.info("Saved LLM analysis for result id={}", id);
        });
    }

    public List<LabResultAnalysisDto> getAnalysisHistory(Long id) {
        return labResultAnalysisRepository.findByLabResultIdOrderByCreatedAtDesc(id)
                .stream()
                .map(a -> LabResultAnalysisDto.builder()
                        .id(a.getId())
                        .analysisText(a.getAnalysisText())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    public List<LabResultDto> getByStatus(ResultStatus status) {
        return labResultRepository.findByStatus(status)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<LabResultDto> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return labResultRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private LabResultDto toDto(LabResult r) {
        List<TestResultDto> tests = testResultRepository.findByLabResultId(r.getId())
                .stream()
                .map(t -> TestResultDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .value(t.getValue())
                        .unit(t.getUnit())
                        .referenceMin(t.getReferenceMin())
                        .referenceMax(t.getReferenceMax())
                        .isAbnormal(t.getIsAbnormal())
                        .build())
                .toList();

        return LabResultDto.builder()
                .id(r.getId())
                .deviceId(r.getDeviceId())
                .patientId(r.getPatientId())
                .timestamp(r.getTimestamp())
                .scenario(r.getScenario())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .llmAnalysis(r.getLlmAnalysis())
                .tests(tests)
                .build();
    }
}