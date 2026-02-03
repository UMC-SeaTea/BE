package com.example.SeaTea.domain.diagnosis.repository;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Long> {

    // 특정 세션 + 멤버 소유 검증용
    Optional<DiagnosisSession> findByIdAndMemberId(Long id, Long memberId);

    // 나의 진단 히스토리 조회 (완료된 세션 전체, 최신순)
    List<DiagnosisSession> findByMemberAndTypeIsNotNullOrderByCreatedAtDesc(Member member);

    // 나의 최신 진단 결과 1건 조회 (완료된 세션 중 최신)
    Optional<DiagnosisSession> findTopByMemberAndTypeIsNotNullOrderByCreatedAtDesc(Member member);
}