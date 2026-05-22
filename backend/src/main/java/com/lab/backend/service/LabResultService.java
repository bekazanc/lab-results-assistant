package com.lab.backend.service;

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
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${mock.service.url}")
    private String mockServiceUrl;

    @Scheduled(fixedDelay = 30000) // her 30 saniyede bir
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

            // Validasyon
            if (!root.hasNonNull("patientId") || !root.hasNonNull("deviceId")) {
                log.warn("Invalid data received - missing required fields. Skipping.");
                return;
            }

            String scenario = root.path("scenario").asText("UNKNOWN");
            List<TestResult> testResults = new ArrayList<>();
            ResultStatus status = ResultStatus.NORMAL;

            JsonNode testsNode = root.path("tests");
            for (JsonNode testNode : testsNode) {
                if (!testNode.hasNonNull("value") ||
                        !testNode.hasNonNull("referenceMin") ||
                        !testNode.hasNonNull("referenceMax")) {
                    continue; // eksik alanları atla
                }

                double value = testNode.path("value").asDouble();
                double min = testNode.path("referenceMin").asDouble();
                double max = testNode.path("referenceMax").asDouble();
                boolean isAbnormal = value < min || value > max;

                if (isAbnormal) {
                    status = scenario.equals("CRITICAL") ? ResultStatus.CRITICAL : ResultStatus.ABNORMAL;
                }

                testResults.add(TestResult.builder()
                        .name(testNode.path("name").asText())
                        .value(value)
                        .unit(testNode.path("unit").asText())
                        .referenceMin(min)
                        .referenceMax(max)
                        .isAbnormal(isAbnormal)
                        .build());
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
                .tests(tests)
                .build();
    }
}