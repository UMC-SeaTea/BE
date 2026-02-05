package com.example.SeaTea.global.auth;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(
      String username
  ) throws UsernameNotFoundException {
    // 검증할 Member 조회
    Member member = memberRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
//        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));
    // CustomUserDetails 반환
    return new CustomUserDetails(member);
  }
}
