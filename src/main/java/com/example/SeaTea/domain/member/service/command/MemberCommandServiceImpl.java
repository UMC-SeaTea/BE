package com.example.SeaTea.domain.member.service.command;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.auth.entity.JwtTokenProvider;
import com.example.SeaTea.global.auth.entity.RefreshToken;
import com.example.SeaTea.global.auth.enums.Role;
import com.example.SeaTea.global.auth.repository.RefreshTokenRepository;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisResponseRepository;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisSessionRepository;
import com.example.SeaTea.global.auth.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final ImageService imageService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final DiagnosisResponseRepository diagnosisResponseRepository;
  private final DiagnosisSessionRepository diagnosisSessionRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${app.cookie.secure}")
  private boolean isSecure;

  @Value("${app.cookie.same-site}")
  private String sameSite;

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
    Member realMember = memberRepository.findById(member.getId())
        .orElseThrow(() -> new MemberException(MemberErrorCode._NOT_FOUND));

    // 닉네임 중복 체크
    checkNicknameDuplication(dto.newNickname());

    // 닉네임 변경 (Dirty Checking 활용)
    member.updateNickname(dto.newNickname());

    // 조회된 영속 상태의 객체 값을 변경
    realMember.updateNickname(dto.newNickname());

    // 가독성이나 즉각적인 반영(save를 호출하지 않아도 @Transactional에 의해 변경 감지(Dirty Checking))
//    memberRepository.save(member);

    return MemberConverter.toUpdateNicknameResultDTO(realMember);
  }

  // 이미지 수정
  @Override
  @Transactional
  public MemberResDTO.UpdateProfileImageResultDTO updateProfileImage(Member member, MemberReqDTO.UpdateProfileImageDTO dto) {
    Member realMember = memberRepository.findById(member.getId())
        .orElseThrow(() -> new MemberException(MemberErrorCode._NOT_FOUND));

    // 기존 파일 삭제
    String oldImageUrl = realMember.getProfile_image();
    if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
      imageService.delete(oldImageUrl);
    }

    // 조회된 영속 상태의 객체 값을 변경
    realMember.updateProfileImage(dto.profileImageUrl());

    // 가독성이나 즉각적인 반영(save를 호출하지 않아도 @Transactional에 의해 변경 감지(Dirty Checking))
//    memberRepository.save(member);

    return MemberConverter.toUpdateProfileImageResultDTO(realMember);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isNicknameDuplicated(String nickname) {
    return memberRepository.existsByNickname(nickname);
  }

  @Override
  @Transactional
  public void withdraw(Member member) {
    // 영속성 컨텍스트에 올리기 위해 조회
    Member realMember = memberRepository.findById(member.getId())
        .orElseThrow(() -> new MemberException(MemberErrorCode._NOT_FOUND));

    // 진단 내역 Soft Delete (탈퇴 시 함께 삭제 처리)
    LocalDateTime now = LocalDateTime.now();
    // 자식(응답) → 부모(세션) 순서로 처리
    diagnosisResponseRepository.softDeleteByMemberId(realMember.getId(), now);
    diagnosisSessionRepository.softDeleteByMemberId(realMember.getId(), now);

    // 프로필 이미지 삭제 (Storage에서 실제 파일 제거)
    String profileImageUrl = realMember.getProfile_image();
    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
      imageService.delete(profileImageUrl);
    }

    // Refresh Token 삭제
    refreshTokenRepository.deleteByUserId(realMember.getId());

    // Unique 제약 조건 충돌 방지를 위한 정보 업데이트
    realMember.prepareForWithdrawal();

    // 기존 이메일&닉네임 변경
    memberRepository.saveAndFlush(realMember);

    // Soft Delete(@SQLDelete에 의해 DELETE가 아닌 UPDATE 쿼리)
    memberRepository.delete(realMember);
  }

  @Override
  @Transactional
  public String reissue(String refreshToken, HttpServletResponse response) {
    // Refresh Token 검증
    if (refreshToken == null || refreshToken.isBlank() || refreshToken.equals("null")) {
      throw new MemberException(MemberErrorCode._JWT_WRONG);
    }

    Authentication authentication;
    try {
      authentication = jwtTokenProvider.getAuthentication(refreshToken);
    } catch (Exception e) {
      // 서명이 아예 다르거나 깨진 토큰일 경우만 예외 처리
      throw new MemberException(MemberErrorCode._JWT_WRONG);
    }

    if (authentication == null || authentication.getName() == null) {
      throw new MemberException(MemberErrorCode._JWT_WRONG);
    }

    // CustomUserDetails의 필드명에 맞춰 memberId를 가져오기
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Long userId = userDetails.getMember().getId(); // 이미 객체에 저장된 ID를 사용 (파싱 불필요)

    // DB에 저장된 Refresh Token 확인
    RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
        .orElseThrow(() -> new MemberException(MemberErrorCode._JWT_WRONG));

    // DB의 토큰과 요청받은 토큰이 일치하는지 비교 (보안상 매우 중요)
    if (!savedToken.getToken().equals(refreshToken)) {
      throw new MemberException(MemberErrorCode._JWT_WRONG);
    }

    // 새로운 Access Token & Refresh Token 생성
    String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

    // DB 업데이트 (Refresh Token Rotation)
    savedToken.updateToken(newRefreshToken);
    refreshTokenRepository.save(savedToken);

    // 새로운 Refresh Token을 쿠키에 설정 (기존 핸들러에서 사용하던 방식대로 응답)
    ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
        .path("/")
        .sameSite(sameSite) // 로컬: LAX 배포: None
        .httpOnly(true)
        .secure(isSecure) // 로컬: false 배포: true
        .maxAge(14 * 24 * 60 * 60) // 14일
        .build();
    response.addHeader("Set-Cookie", cookie.toString());

    return newAccessToken;
  }
}
