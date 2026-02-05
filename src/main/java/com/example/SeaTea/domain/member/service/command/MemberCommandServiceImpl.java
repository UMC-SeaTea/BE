package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.auth.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  // 회원가입
  @Override
  @Transactional
  public MemberResDTO.JoinDTO signup(
      MemberReqDTO.JoinDTO dto
  ){
    // 비밀번호 일치 확인
    if (!dto.password().equals(dto.passwordConfirm())) {
      throw new RuntimeException("비밀번호가 일치하지 않습니다.");
    }
    // 이메일 중복 재확인 (보안상 한 번 더 체크)
    checkEmailDuplication(dto.email());

    // 솔트된 비밀번호 생성(salt+hash가 합쳐진 비밀번호 문자열)
    String salt = passwordEncoder.encode(dto.password());

    // 사용자 생성
    Member member = MemberConverter.toMember(dto, salt, Role.ROLE_MEMBER);
    // DB 적용
    memberRepository.save(member);

    // 응답 DTO 생성
    return MemberConverter.toJoinDTO(member);
  }

  @Override
  @Transactional(readOnly = true)
  public void checkEmailDuplication(String email) {
    if (memberRepository.existsByEmail(email)) {
      // 커스텀 예외를 정의하여 사용하는 것을 추천합니다.
      // 여기서는 예시로 RuntimeException을 사용합니다.
      throw new RuntimeException("이미 사용 중인 이메일입니다.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void checkNicknameDuplication(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      // 커스텀 예외를 정의하여 사용하는 것을 추천합니다.
      // 여기서는 예시로 RuntimeException을 사용합니다.
      throw new RuntimeException("이미 사용 중인 닉네임입니다.");
    }
  }

}
