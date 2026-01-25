package com.example.SeaTea.domain.diagnosis.repository;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Long> {
    Optional<DiagnosisSession> findByIdAndMemberId(Long id, Long memberId);
}