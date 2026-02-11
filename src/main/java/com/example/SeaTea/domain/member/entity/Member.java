package com.example.SeaTea.domain.member.entity;

import com.example.SeaTea.global.auth.enums.Role;
import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "member")
public class Member extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "memberId")
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = true)
  private String password;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column
  private String profile_image;

//  role 인가처리
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role; // USER, ADMIN


  // 소셜 로그인 식별 정보
  private String registrationId; // "kakao" 저장
  private String providerId;     // 카카오의 고유 식별 번호 저장
  private boolean isNewUser = false; // 회원가입 시 신규 유저인지 확인(기본 false)

  // 회원가입 시점에 true로 변경
  public void markAsNewUser() {
    this.isNewUser = true;
  }

  // 회원가입 후 false로 변경
  public void offNewUser() {
    this.isNewUser = false;
  }

  // 소셜 로그인 전용 빌더/생성자 혹은 업데이트 메서드
  public Member updateSocialInfo(String registrationId, String providerId) {
    this.registrationId = registrationId;
    this.providerId = providerId;
    return this;
  }

  // 닉네임 업데이트
  public void updateNickname(String newNickname) {
    this.nickname = newNickname;
  }

  // 이미지 업데이트
  public void updateProfileImage(String profileImageUrl) {
    this.profile_image = profileImageUrl;
  }




//  status 탈퇴/차단 처리
//  @Enumerated(EnumType.STRING)
//  private MemberStatus status; // ACTIVE, BLOCKED, DELETED
}
