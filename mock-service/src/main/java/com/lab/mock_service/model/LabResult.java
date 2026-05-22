package com.lab.mock_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabResult {
    private String deviceId;
    private String patientId;
    private String timestamp;
    private String scenario;
    private List<TestResult> tests;
}