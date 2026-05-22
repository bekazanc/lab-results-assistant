package com.lab.backend.repository;

import com.lab.backend.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByLabResultId(Long labResultId);
    List<TestResult> findByIsAbnormalTrue();
}