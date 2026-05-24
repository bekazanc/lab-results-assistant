package com.lab.mock_service.service;

import com.lab.mock_service.model.LabResult;
import com.lab.mock_service.model.TestResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class LabResultGenerator {

    private final Random random = new Random();

    public Object generate() {
        int scenario = random.nextInt(8);
        return switch (scenario) {
            case 0 -> buildResult("NORMAL",
                    glucose(80, 95), hba1c(4.5, 5.5), hemoglobin(13.5, 16.0), wbc(5.0, 9.0));
            case 1 -> buildResult("ABNORMAL_HIGH",
                    glucose(180, 320), hba1c(7.5, 11.0), hemoglobin(13.5, 16.0), wbc(5.0, 9.0));
            case 2 -> buildResult("CRITICAL",
                    glucose(450, 600), hba1c(12.0, 14.0), hemoglobin(6.0, 7.5), wbc(25.0, 40.0));
            case 3 -> buildMissingFieldResult();
            case 4 -> buildResult("ABNORMAL_LOW",
                    glucose(45, 60), hba1c(4.5, 5.5), hemoglobin(7.0, 9.5), wbc(1.5, 2.8));
            case 5 -> buildEmptyTestsResult();
            case 6 -> buildNegativeValueResult();
            case 7 -> buildDuplicateTestResult();
            default -> buildResult("NORMAL",
                    glucose(80, 95), hba1c(4.5, 5.5), hemoglobin(13.5, 16.0), wbc(5.0, 9.0));
        };
    }

    private LabResult buildResult(String scenario, TestResult... tests) {
        return LabResult.builder()
                .deviceId("LAB-00" + (random.nextInt(3) + 1))
                .patientId("P-" + (1000 + random.nextInt(9000)))
                .timestamp(LocalDateTime.now().toString())
                .scenario(scenario)
                .tests(List.of(tests))
                .build();
    }

    // Senaryo: eksik alan
    private Map<String, Object> buildMissingFieldResult() {
        return Map.of(
                "deviceId", "LAB-999",
                "timestamp", LocalDateTime.now().toString(),
                "scenario", "INVALID_MISSING_FIELDS",
                "tests", List.of(
                        Map.of("name", "Glucose", "value", 95.0, "unit", "mg/dL")
                )
        );
    }

    // Senaryo: boş test listesi
    private Map<String, Object> buildEmptyTestsResult() {
        return Map.of(
                "deviceId", "LAB-00" + (random.nextInt(3) + 1),
                "patientId", "P-" + (1000 + random.nextInt(9000)),
                "timestamp", LocalDateTime.now().toString(),
                "scenario", "INVALID_EMPTY_TESTS",
                "tests", List.of()
        );
    }

    // Senaryo: negatif değer
    private Map<String, Object> buildNegativeValueResult() {
        return Map.of(
                "deviceId", "LAB-00" + (random.nextInt(3) + 1),
                "patientId", "P-" + (1000 + random.nextInt(9000)),
                "timestamp", LocalDateTime.now().toString(),
                "scenario", "INVALID_NEGATIVE_VALUE",
                "tests", List.of(
                        Map.of("name", "Glucose", "value", -45.0,
                                "unit", "mg/dL", "referenceMin", 70.0, "referenceMax", 100.0),
                        Map.of("name", "HbA1c", "value", 5.2,
                                "unit", "%", "referenceMin", 4.0, "referenceMax", 5.7)
                )
        );
    }

    // Senaryo: duplicate test
    private Map<String, Object> buildDuplicateTestResult() {
        return Map.of(
                "deviceId", "LAB-00" + (random.nextInt(3) + 1),
                "patientId", "P-" + (1000 + random.nextInt(9000)),
                "timestamp", LocalDateTime.now().toString(),
                "scenario", "INVALID_DUPLICATE_TEST",
                "tests", List.of(
                        Map.of("name", "Glucose", "value", 85.0,
                                "unit", "mg/dL", "referenceMin", 70.0, "referenceMax", 100.0),
                        Map.of("name", "Glucose", "value", 92.0,
                                "unit", "mg/dL", "referenceMin", 70.0, "referenceMax", 100.0),
                        Map.of("name", "HbA1c", "value", 5.2,
                                "unit", "%", "referenceMin", 4.0, "referenceMax", 5.7)
                )
        );
    }

    private TestResult glucose(double min, double max) {
        return TestResult.builder()
                .name("Glucose").value(randBetween(min, max))
                .unit("mg/dL").referenceMin(70.0).referenceMax(100.0).build();
    }

    private TestResult hba1c(double min, double max) {
        return TestResult.builder()
                .name("HbA1c").value(randBetween(min, max))
                .unit("%").referenceMin(4.0).referenceMax(5.7).build();
    }

    private TestResult hemoglobin(double min, double max) {
        return TestResult.builder()
                .name("Hemoglobin").value(randBetween(min, max))
                .unit("g/dL").referenceMin(12.0).referenceMax(17.5).build();
    }

    private TestResult wbc(double min, double max) {
        return TestResult.builder()
                .name("WBC").value(randBetween(min, max))
                .unit("10³/µL").referenceMin(4.5).referenceMax(11.0).build();
    }

    private double randBetween(double min, double max) {
        return Math.round((min + random.nextDouble() * (max - min)) * 10.0) / 10.0;
    }
}