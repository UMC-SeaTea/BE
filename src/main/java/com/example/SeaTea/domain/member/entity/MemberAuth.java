package com.example.SeaTea.domain.member.entity;

import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class MemberAuth extends BaseEntity {
  @Id
  @GeneratedValue
  private Long id;

//  JoinColumn -> FK의 주인 설정 / 단방향 1:N 매핑
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "memberId")
  private Member member;

  // SeaTea에서는 KAKAO 소셜 로그인 사용 예정
  @Column(nullable = false)
  private String provider; // GOOGLE, KAKAO

//  provider	providerId
//  KAKAO	329485093
  @Column(nullable = false)
  private String providerId; // OAuth 제공자가 준 ID

  @Column
  private String email;
}
