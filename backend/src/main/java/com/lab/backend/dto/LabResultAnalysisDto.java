package com.lab.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabResultAnalysisDto {
    private Long id;
    private String analysisText;
    private LocalDateTime createdAt;
}