package com.lab.backend.repository;

import com.lab.backend.model.LabResultAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabResultAnalysisRepository extends JpaRepository<LabResultAnalysis, Long> {
    List<LabResultAnalysis> findByLabResultIdOrderByCreatedAtDesc(Long labResultId);
}