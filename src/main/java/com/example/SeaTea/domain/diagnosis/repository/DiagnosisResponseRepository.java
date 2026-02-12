package com.example.SeaTea.domain.diagnosis.repository;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface DiagnosisResponseRepository extends JpaRepository<DiagnosisResponse, Long> {
    List<DiagnosisResponse> findAllBySessionId(Long sessionId);

    //소프트 딜리트 -> session으로 조인한뒤 업데이트
    @Modifying
    @Transactional
    @Query(value = """
      UPDATE diagnosis_response dr
      JOIN diagnosis_session ds ON dr.session_id = ds.id
      SET dr.deleted_at = :now
      WHERE ds.member_id = :memberId
        AND dr.deleted_at IS NULL
      """, nativeQuery = true)
    int softDeleteByMemberId(Long memberId, LocalDateTime now);
}