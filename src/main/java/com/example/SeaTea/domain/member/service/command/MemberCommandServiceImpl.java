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
      MemberReqDTO.JoinDTO dto,
      MemberReqDTO.ProfileDTO profDto
  ){
    // 솔트된 비밀번호 생성(salt+hash가 합쳐진 비밀번호 문자열)
    String salt = passwordEncoder.encode(dto.password());

    // 사용자 생성
    Member member = MemberConverter.toMember(dto, salt, Role.ROLE_MEMBER, profDto);
    // DB 적용
    memberRepository.save(member);

    // 응답 DTO 생성
    return MemberConverter.toJoinDTO(member);
  }

}
