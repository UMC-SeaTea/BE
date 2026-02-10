package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
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
      throw new MemberException(MemberErrorCode._DIFFERENT_PW);
    }
    // 이메일 중복 재확인 (보안상 한 번 더 체크)
    checkEmailDuplication(dto.email());
    // 닉네임도 중복 확인
    checkNicknameDuplication(dto.nickname());

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
      throw new MemberException(MemberErrorCode._CONFLICT_EMAIL);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void checkNicknameDuplication(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new MemberException(MemberErrorCode._CONFLICT_NICKNAME);
    }
  }

  @Override
  @Transactional
  public MemberResDTO.UpdateNicknameResultDTO updateNickname(Member member, MemberReqDTO.UpdateNicknameDTO dto) {
    // 닉네임 중복 체크
    checkNicknameDuplication(dto.newNickname());

    // 닉네임 변경 (Dirty Checking 활용)
    member.updateNickname(dto.newNickname());

    // 가독성이나 즉각적인 반영(save를 호출하지 않아도 @Transactional에 의해 변경 감지(Dirty Checking))
    memberRepository.save(member);

    return MemberConverter.toUpdateNicknameResultDTO(member);
  }

}
