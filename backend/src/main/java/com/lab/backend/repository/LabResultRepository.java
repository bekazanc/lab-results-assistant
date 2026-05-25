package com.lab.backend.repository;

import com.lab.backend.model.LabResult;
import com.lab.backend.model.ResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByStatus(ResultStatus status);
    List<LabResult> findByPatientIdContainingIgnoreCase(String patientId);
    List<LabResult> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}