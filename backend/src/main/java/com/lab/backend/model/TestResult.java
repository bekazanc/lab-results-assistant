package com.lab.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double value;
    private String unit;
    private Double referenceMin;
    private Double referenceMax;
    private Boolean isAbnormal;

    @ManyToOne
    @JoinColumn(name = "lab_result_id")
    private LabResult labResult;
}