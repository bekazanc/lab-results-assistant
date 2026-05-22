package com.lab.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResultDto {
    private Long id;
    private String name;
    private Double value;
    private String unit;
    private Double referenceMin;
    private Double referenceMax;
    private Boolean isAbnormal;
}