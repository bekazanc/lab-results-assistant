package com.lab.backend.dto;

import com.lab.backend.model.ResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabResultDto {
    private Long id;
    private String deviceId;
    private String patientId;
    private LocalDateTime timestamp;
    private String scenario;
    private ResultStatus status;
    private LocalDateTime createdAt;
    private List<TestResultDto> tests;
    private String llmAnalysis;
}