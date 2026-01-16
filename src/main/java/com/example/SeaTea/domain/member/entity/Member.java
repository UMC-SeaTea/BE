package com.example.SeaTea.domain.member.entity;

import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
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

//  role 인가처리
//  @Enumerated(EnumType.STRING)
//  private Role role; // USER, ADMIN

//  status 탈퇴/차단 처리
//  @Enumerated(EnumType.STRING)
//  private MemberStatus status; // ACTIVE, BLOCKED, DELETED
}
