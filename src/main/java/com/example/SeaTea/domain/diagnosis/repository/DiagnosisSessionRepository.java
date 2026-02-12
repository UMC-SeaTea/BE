package com.example.SeaTea.domain.diagnosis.repository;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiagnosisSessionRepository extends JpaRepository<DiagnosisSession, Long> {

    // 특정 세션 + 멤버 소유 검증용
    Optional<DiagnosisSession> findByIdAndMemberId(Long id, Long memberId);

    // 나의 최신 진단 결과 1건 조회 (완료된 세션 중 최신)
    Optional<DiagnosisSession> findTopByMemberAndTypeIsNotNullOrderByCreatedAtDesc(Member member);

    // 나의 최신 진단 결과 1건 조회 (완료된 세션 중 최신) - soft delete 제외
    Optional<DiagnosisSession> findTopByMemberAndDeletedAtIsNullAndTypeIsNotNullOrderByCreatedAtDesc(Member member);

    // 나의 진단 히스토리 조회 (완료된 세션 전체, 최신순)
    Slice<DiagnosisSession> findByMemberAndTypeIsNotNullOrderByCreatedAtDesc(Member member, Pageable pageable);

    // 나의 진단 히스토리 조회 (완료된 세션 전체, 최신순) - soft delete 제외
    Slice<DiagnosisSession> findByMemberAndDeletedAtIsNullAndTypeIsNotNullOrderByCreatedAtDesc(Member member, Pageable pageable);

    //소프트 딜리트
    @Modifying
    @Transactional
    @Query(value = """
      UPDATE diagnosis_session
      SET deleted_at = :now
      WHERE member_id = :memberId
        AND deleted_at IS NULL
      """, nativeQuery = true)
    int softDeleteByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}