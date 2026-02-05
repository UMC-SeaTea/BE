package com.example.SeaTea.domain.member.repository;

import com.example.SeaTea.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  // 닉네임 중복 확인
  boolean existsByNickname(String nickname);
}
