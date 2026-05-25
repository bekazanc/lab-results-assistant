package com.lab.backend;

import com.lab.backend.model.LabResult;
import com.lab.backend.model.ResultStatus;
import com.lab.backend.repository.LabResultAnalysisRepository;
import com.lab.backend.repository.LabResultRepository;
import com.lab.backend.repository.TestResultRepository;
import com.lab.backend.service.LabResultService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LabResultServiceTest {

    @Mock
    private LabResultRepository labResultRepository;

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private LabResultAnalysisRepository labResultAnalysisRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LabResultService labResultService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        labResultService = new LabResultService(
                labResultRepository,
                testResultRepository,
                labResultAnalysisRepository,
                objectMapper,
                restTemplate
        );
    }

    @Test
    void testProcessAndSave_ValidNormalResult() {
        String json = """
            {
                "deviceId": "LAB-001",
                "patientId": "P-1234",
                "timestamp": "2026-05-25T10:00:00",
                "scenario": "NORMAL",
                "tests": [
                    {"name": "Glucose", "value": 85.0, "unit": "mg/dL", "referenceMin": 70.0, "referenceMax": 100.0},
                    {"name": "HbA1c", "value": 5.2, "unit": "%", "referenceMin": 4.0, "referenceMax": 5.7},
                    {"name": "Hemoglobin", "value": 14.0, "unit": "g/dL", "referenceMin": 12.0, "referenceMax": 17.5},
                    {"name": "WBC", "value": 7.0, "unit": "10³/µL", "referenceMin": 4.5, "referenceMax": 11.0}
                ]
            }
            """;

        LabResult mockSaved = LabResult.builder()
                .id(1L)
                .patientId("P-1234")
                .status(ResultStatus.NORMAL)
                .build();

        when(labResultRepository.save(any())).thenReturn(mockSaved);
        when(testResultRepository.saveAll(any())).thenReturn(List.of());

        labResultService.processAndSave(json);

        verify(labResultRepository, times(1)).save(any());
        verify(testResultRepository, times(1)).saveAll(any());
    }

    @Test
    void testProcessAndSave_MissingPatientId_ShouldSkip() {
        String json = """
            {
                "deviceId": "LAB-999",
                "timestamp": "2026-05-25T10:00:00",
                "scenario": "INVALID_MISSING_FIELDS",
                "tests": []
            }
            """;

        labResultService.processAndSave(json);

        verify(labResultRepository, never()).save(any());
    }

    @Test
    void testProcessAndSave_EmptyTests_ShouldSkip() {
        String json = """
            {
                "deviceId": "LAB-001",
                "patientId": "P-1234",
                "timestamp": "2026-05-25T10:00:00",
                "scenario": "INVALID_EMPTY_TESTS",
                "tests": []
            }
            """;

        labResultService.processAndSave(json);

        verify(labResultRepository, never()).save(any());
    }

    @Test
    void testProcessAndSave_NegativeValue_ShouldSkip() {
        String json = """
            {
                "deviceId": "LAB-001",
                "patientId": "P-1234",
                "timestamp": "2026-05-25T10:00:00",
                "scenario": "INVALID_NEGATIVE_VALUE",
                "tests": [
                    {"name": "Glucose", "value": -45.0, "unit": "mg/dL", "referenceMin": 70.0, "referenceMax": 100.0}
                ]
            }
            """;

        labResultService.processAndSave(json);

        verify(labResultRepository, never()).save(any());
    }

    @Test
    void testProcessAndSave_CriticalResult_ShouldSaveWithCriticalStatus() {
        String json = """
            {
                "deviceId": "LAB-001",
                "patientId": "P-1234",
                "timestamp": "2026-05-25T10:00:00",
                "scenario": "CRITICAL",
                "tests": [
                    {"name": "Glucose", "value": 550.0, "unit": "mg/dL", "referenceMin": 70.0, "referenceMax": 100.0},
                    {"name": "HbA1c", "value": 13.0, "unit": "%", "referenceMin": 4.0, "referenceMax": 5.7},
                    {"name": "Hemoglobin", "value": 7.0, "unit": "g/dL", "referenceMin": 12.0, "referenceMax": 17.5},
                    {"name": "WBC", "value": 30.0, "unit": "10³/µL", "referenceMin": 4.5, "referenceMax": 11.0}
                ]
            }
            """;

        LabResult mockSaved = LabResult.builder()
                .id(1L)
                .patientId("P-1234")
                .status(ResultStatus.CRITICAL)
                .build();

        when(labResultRepository.save(any())).thenReturn(mockSaved);
        when(testResultRepository.saveAll(any())).thenReturn(List.of());

        labResultService.processAndSave(json);

        verify(labResultRepository, times(1)).save(argThat(result ->
                result.getStatus() == ResultStatus.CRITICAL
        ));
    }
}