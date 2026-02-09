// 한 명의 사용자가 여러 종류의 로그인을 사용할 경우 사용자와 사용자 인증 테이블을 1:N 매핑으로 둬서
// 카카오 회원가입을 했다가 추후 비밀번호도 추가하여 소셜 로그인도 진행할 수 있도록 확장 가능

//package com.example.SeaTea.domain.member.entity;
//
//import com.example.SeaTea.global.common.BaseEntity;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//
//@Entity
//public class MemberAuth extends BaseEntity {
//  @Id
//  @GeneratedValue
//  private Long id;
//
// //  JoinColumn -> FK의 주인 설정 / 단방향 1:N 매핑
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "memberId")
//  private Member member;
//
//  // SeaTea에서는 KAKAO 소셜 로그인 사용 예정
//  @Column(nullable = false)
//  private String provider; // GOOGLE, KAKAO
//
// //  provider	providerId
// //  KAKAO	329485093
//  @Column(nullable = false)
//  private String providerId; // OAuth 제공자가 준 ID
//
//  @Column
//  private String email;
//}
