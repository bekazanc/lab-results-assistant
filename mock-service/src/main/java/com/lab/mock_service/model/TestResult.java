package com.lab.mock_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    private String name;
    private Double value;
    private String unit;
    private Double referenceMin;
    private Double referenceMax;
}