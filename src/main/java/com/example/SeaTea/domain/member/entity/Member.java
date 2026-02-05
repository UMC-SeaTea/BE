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

@Entity
@Getter
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

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column
  private String profile_image;

//  role 인가처리
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role; // USER, ADMIN

//  status 탈퇴/차단 처리
//  @Enumerated(EnumType.STRING)
//  private MemberStatus status; // ACTIVE, BLOCKED, DELETED
}
