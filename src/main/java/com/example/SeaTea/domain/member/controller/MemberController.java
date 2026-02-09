package com.example.SeaTea.domain.member.controller;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.domain.member.exception.code.MemberSuccessCode;
import com.example.SeaTea.domain.member.service.command.MemberCommandService;
import com.example.SeaTea.domain.member.service.query.MemberQueryService;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.CustomUserDetails;
import com.example.SeaTea.global.status.SuccessStatus;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 테스트
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

  private final MemberQueryService memberQueryService;
  private final MemberCommandService memberCommandService;

  // 회원가입 정보 입력 페이지
  @GetMapping("/sign-up")
  public String signUpForm() {
    return "/api/sign-up";
  }

  @PostMapping("/sign-up")
  public ApiResponse<MemberResDTO.JoinDTO> signup(
      @RequestBody @Valid MemberReqDTO.JoinDTO dto
  ) {
    return ApiResponse.of(MemberSuccessCode._CREATED, memberCommandService.signup(dto));
  }


  // ******** 중복체크
  @GetMapping("/check/email")
  public ApiResponse<String> checkEmail(@RequestParam String email) {
    memberCommandService.checkEmailDuplication(email);
    return ApiResponse.onSuccess("사용 가능한 이메일입니다.");
  }
  @GetMapping("/check/nickname")
  public ApiResponse<String> checkNickname(@RequestParam String nickname) {
    memberCommandService.checkNicknameDuplication(nickname);
    return ApiResponse.onSuccess("사용 가능한 닉네임입니다.");
  }

  @GetMapping("/users/me")
  public ApiResponse<MemberResDTO.LoginDTO> getMyInfo(@AuthenticationPrincipal Object principal) {
    if (principal == null) {
      // 예외처리 통일을 위해 ApiResponse로 처리
      // throw new MemberException(MemberErrorCode._NOT_LOGIN);
      return ApiResponse.onFailure(MemberErrorCode._NOT_LOGIN.getCode(),MemberErrorCode._NOT_LOGIN.getMessage(),null);
    }

    String email;
    String nickname;
    String role;

    if (principal instanceof CustomUserDetails userDetails) {
      // 일반 로그인 유저
      email = userDetails.getMember().getEmail();
      nickname = userDetails.getMember().getNickname();
      role = userDetails.getMember().getRole().toString();
    } else if (principal instanceof OAuth2User oAuth2User) {
      // 소셜 로그인 유저 (Map 파싱)
      Map<String, Object> attributes = oAuth2User.getAttributes();
      nickname = "소셜 유저";

      // 카카오 구조에 따른 안전한 파싱
      if (attributes.get("kakao_account") instanceof Map<?, ?> kakaoAccount) {
        email = (String) kakaoAccount.get("email");
        if (kakaoAccount.get("profile") instanceof Map<?, ?> profile) {
          nickname = (String) profile.get("nickname");
        }
      } else {
        // 카카오가 아닌 다른 소셜 서비스일 경우의 기본 파싱
        email = (String) attributes.get("email");
      }

      // 소셜 유저의 권한 추출 (SecurityContext에 설정된 권한 기준)
      role = oAuth2User.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .findFirst()
          .orElse("ROLE_MEMBER");
    } else {
      // 로그인 되지 않은 상태
      return ApiResponse.onFailure(MemberErrorCode._NOT_LOGIN.getCode(),MemberErrorCode._NOT_LOGIN.getMessage(),null);
//      return ApiResponse.onFailure(MemberErrorCode._INVALID_LOGIN_TYPE.getCode(),MemberErrorCode._INVALID_LOGIN_TYPE.getMessage(),null);
    }

    MemberResDTO.LoginDTO result = MemberResDTO.LoginDTO.builder()
        .email(email)
        .nickname(nickname)
        .role(role)
        .build();

    // 로그인 완료
    return ApiResponse.of(MemberSuccessCode._FOUND, result);
  }

  // 관리자 테스트
  @GetMapping("/admin/test")
  public ApiResponse<MemberResDTO.Tasting> test() throws Exception {
    // 응답 코드 정의
    SuccessStatus code = SuccessStatus._OK;
//    throw new MemberException(ErrorStatus._INTERNAL_SERVER_ERROR);
    return ApiResponse.onSuccess(MemberConverter.toTestingDTO("관리자 계정입니다!"));
  }

  // 예외 상황
//  @GetMapping("/exception")
//  public ApiResponse<MemberResDTO.Exceptions> exception(
//      @RequestParam Long flag
//  ) {
//    memberQueryService.checkFlag(flag);
//
//    // 응답 코드 정의
//    SuccessStatus code = SuccessStatus._OK;
//    return ApiResponse.onSuccess(MemberConverter.toExceptionsDTO("I'm testing"));
//  }
}
