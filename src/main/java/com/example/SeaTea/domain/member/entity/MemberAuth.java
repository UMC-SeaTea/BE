package com.example.SeaTea.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAuth {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  // SeaTea에서는 KAKAO 소셜 로그인 사용 예정
  @Column(nullable = false)
  private String provider; // GOOGLE, KAKAO

  @Column(nullable = false)
  private String providerId; // OAuth 제공자가 준 ID

  @Column
  private String email;
}
