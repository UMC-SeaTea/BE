package com.example.SeaTea.domain.member.controller;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.code.MemberErrorCode;
import com.example.SeaTea.domain.member.exception.code.MemberSuccessCode;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.domain.member.service.command.ImageService;
import com.example.SeaTea.domain.member.service.command.MemberCommandService;
import com.example.SeaTea.domain.member.service.query.MemberQueryService;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.service.CustomUserDetails;
import com.example.SeaTea.global.status.SuccessStatus;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// 테스트
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

  private final MemberCommandService memberCommandService;
  private final MemberQueryService memberQueryService;
  private final MemberRepository memberRepository;
  private final ImageService imageService;

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

    if (principal instanceof CustomUserDetails userDetails) {
      // 일반 로그인 유저
      Member member = userDetails.getMember();

      return ApiResponse.of(MemberSuccessCode._FOUND, MemberConverter.toLoginDTO(member));

    } else if (principal instanceof OAuth2User oAuth2User) {
      // 소셜 로그인 유저 (Map 파싱)
      Map<String, Object> attributes = oAuth2User.getAttributes();

      String role;
      String email;
      String nickname = "소셜 유저";

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

      MemberResDTO.LoginDTO result = MemberResDTO.LoginDTO.builder()
          .email(email)
          .nickname(nickname)
          .role(role)
          .build();

      return ApiResponse.of(MemberSuccessCode._FOUND, result);

    } else {
      // 로그인 되지 않은 상태
      return ApiResponse.onFailure(MemberErrorCode._NOT_LOGIN.getCode(),MemberErrorCode._NOT_LOGIN.getMessage(),null);
//      return ApiResponse.onFailure(MemberErrorCode._INVALID_LOGIN_TYPE.getCode(),MemberErrorCode._INVALID_LOGIN_TYPE.getMessage(),null);
    }
  }

  // 닉네임 변경 api
  @PatchMapping("/users/me/change/nickname")
  public ApiResponse<MemberResDTO.UpdateNicknameResultDTO> updateNickname(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid MemberReqDTO.UpdateNicknameDTO dto
  ) {
    if (userDetails == null) {
      return ApiResponse.onFailure(MemberErrorCode._NOT_LOGIN.getCode(), MemberErrorCode._NOT_LOGIN.getMessage(), null);
    }

    Member member = userDetails.getMember();

    // 닉네임 중복 체크 (ApiResponse로 에러 반환)
    if (memberCommandService.isNicknameDuplicated(dto.newNickname())) {
      return ApiResponse.onFailure(MemberErrorCode._CONFLICT_NICKNAME.getCode(), MemberErrorCode._CONFLICT_NICKNAME.getMessage(), null);
    }

    return ApiResponse.onSuccess(memberCommandService.updateNickname(member, dto));
  }


  // 이미지 업로드 api
  @PostMapping("/upload/profile/image")
  public ApiResponse<String> uploadProfileImage(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return ApiResponse.onFailure(MemberErrorCode._FILE_EMPTY.getCode(), MemberErrorCode._FILE_EMPTY.getMessage(), null);
    }
    String imageUrl = imageService.upload(file);
    return ApiResponse.onSuccess(imageUrl);
  }

  // 이미지 변경 api (DB 업데이트용)
  @PatchMapping("/users/me/change/profile/image")
  public ApiResponse<MemberResDTO.UpdateProfileImageResultDTO> updateProfileImage(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid MemberReqDTO.UpdateProfileImageDTO dto // 💡 URL을 받을 DTO 필요
  ) {
    if (userDetails == null) {
      return ApiResponse.onFailure(MemberErrorCode._NOT_LOGIN.getCode(), MemberErrorCode._NOT_LOGIN.getMessage(), null);
    }

    Member member = userDetails.getMember();

    // 서비스에서 기존 파일 삭제 + DB URL 업데이트 수행
    return ApiResponse.onSuccess(memberCommandService.updateProfileImage(member, dto));
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

  // 소셜 로그인한 계정 이메일 가져오기
  private String extractEmailFromOAuth2User(OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();

    // 카카오 로그인일 경우의 처리 (이미 작성하신 getMyInfo 로직의 구조를 참고함)
    if (attributes.get("kakao_account") instanceof Map<?, ?> kakaoAccount) {
      return (String) kakaoAccount.get("email");
    }

    // 구글이나 기타 서비스일 경우 (기본적으로 "email" 키를 사용한다고 가정)
    Object email = attributes.get("email");
    if (email == null) {
      // 이메일 권한이 없거나 식별할 수 없는 경우에 대한 처리 (Speculation: 추측)
      return null;
    }

    return email.toString();
  }

  @GetMapping("/users/profile")
  public ApiResponse<MemberResDTO.MemberInfoDTO> getMemberProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    if (userDetails == null) {
      return ApiResponse.onFailure(
          MemberErrorCode._NOT_LOGIN.getCode(),
          MemberErrorCode._NOT_LOGIN.getMessage(),
          null
      );
    }

    // 서비스 호출 시 ID를 넘겨 최신 정보를 DB에서 조회 (영속성 컨텍스트 보장)
    MemberResDTO.MemberInfoDTO result = memberQueryService.getMemberInfo(userDetails.getMember().getId());

    return ApiResponse.onSuccess(result);
  }
}
