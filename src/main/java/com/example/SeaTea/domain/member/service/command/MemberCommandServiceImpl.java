package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.auth.enums.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public MemberResDTO.JoinDTO signup(
      MemberReqDTO.JoinDTO dto
  ){
    // 솔트된 비밀번호 생성
    String salt = passwordEncoder.encode(dto.password());

    // 사용자 생성
    Member member = MemberConverter.toMember(dto, salt, Role.ROLE_MEMBER);
    // DB 적용
    memberRepository.save(member);

    // 응답 DTO 생성
    return MemberConverter.toJoinDTO(member);
  }

}
