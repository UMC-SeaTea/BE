package com.example.SeaTea.global.auth.service;

import com.example.SeaTea.domain.member.entity.Member;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomUserDetails implements UserDetails, OAuth2User {

  @Getter
  private final Member member;
  private Map<String, Object> attributes; // 소셜 로그인용 속성 저장

  // 일반 로그인용 생성자
  public CustomUserDetails(Member member) {
    this.member = member;
  }

  // 소셜 로그인용 생성자
  public CustomUserDetails(Member member, Map<String, Object> attributes) {
    this.member = member;
    this.attributes = attributes;
  }


  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return member.getEmail();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(() -> member.getRole().toString());
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public String getUsername() {
    return member.getEmail();
  }

}
