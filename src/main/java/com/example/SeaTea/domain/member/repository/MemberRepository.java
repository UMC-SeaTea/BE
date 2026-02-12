package com.example.SeaTea.domain.member.repository;

import com.example.SeaTea.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  // 이메일 중복 확인
  boolean existsByEmail(String email);

  // 닉네임 중복 확인
  boolean existsByNickname(String nickname);

  // deleted_at이 before 이전인 데이터를 물리적으로 삭제
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM member WHERE deleted_at <= :before", nativeQuery = true)
  void deleteByDeletedAtBefore(LocalDateTime before);
}
