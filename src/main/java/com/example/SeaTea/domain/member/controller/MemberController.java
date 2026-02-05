package com.example.SeaTea.domain.member.controller;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.exception.code.MemberSuccessCode;
import com.example.SeaTea.domain.member.service.command.MemberCommandService;
import com.example.SeaTea.domain.member.service.query.MemberQueryService;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.CustomUserDetails;
import com.example.SeaTea.global.status.SuccessStatus;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  @PostMapping("/sign-up")
  public ApiResponse<MemberResDTO.JoinDTO> signup(
      @RequestBody MemberReqDTO.JoinDTO dto,
      @RequestBody MemberReqDTO.ProfileDTO profDto
  ) {
    return ApiResponse.of(MemberSuccessCode.FOUND, memberCommandService.signup(dto, profDto));
  }

  @GetMapping("/users/me")
  public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    if (customUserDetails == null) {
      return ResponseEntity.status(401).body("로그인 상태가 아닙니다.");
    }

    Member member = customUserDetails.getMember();

    // 필요한 정보만 담아서 응답 (보안상 비밀번호 등은 제외)
    Map<String, Object> response = new HashMap<>();
    response.put("email", member.getEmail());
    response.put("nickname", member.getNickname()); // Member 엔티티에 닉네임이 있다고 가정
    response.put("role", member.getRole());

    return ResponseEntity.ok(response);
  }

  // 관리자 테스트
  @GetMapping("/admin/test")
  public ApiResponse<MemberResDTO.Tasting> test() throws Exception {
    // 응답 코드 정의
    SuccessStatus code = SuccessStatus._OK;
//    throw new MemberException(ErrorStatus._INTERNAL_SERVER_ERROR);
    return ApiResponse.onSuccess(
        MemberConverter.toTestingDTO("관리자 계정입니다!")
    );
  }

  // 예외 상황
  @GetMapping("/exception")
  public ApiResponse<MemberResDTO.Exceptions> exception(
      @RequestParam Long flag
  ) {
    memberQueryService.checkFlag(flag);

    // 응답 코드 정의
    SuccessStatus code = SuccessStatus._OK;
    return ApiResponse.onSuccess(MemberConverter.toExceptionsDTO("I'm testing"));
  }
}
