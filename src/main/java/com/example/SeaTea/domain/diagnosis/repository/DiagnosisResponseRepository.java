package com.example.SeaTea.domain.diagnosis.repository;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisResponseRepository extends JpaRepository<DiagnosisResponse, Long> {
    List<DiagnosisResponse> findAllBySessionId(Long sessionId);
}