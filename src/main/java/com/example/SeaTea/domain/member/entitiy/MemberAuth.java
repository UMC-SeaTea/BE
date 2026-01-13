package com.example.SeaTea.domain.member.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class MemberAuth {
  @Id
  @GeneratedValue
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  // SeaTea에서는 KAKAO 소셜 로그인 사용 예정
  @Column(nullable = false)
  private String provider; // GOOGLE, KAKAO

  @Column(nullable = false)
  private String providerId; // OAuth 제공자가 준 ID

  @Column
  private String email;
}
