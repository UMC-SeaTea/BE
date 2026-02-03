package com.example.SeaTea.domain.member.controller;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.request.MemberReqDTO;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.domain.member.exception.MemberException;
import com.example.SeaTea.domain.member.exception.code.MemberSuccessCode;
import com.example.SeaTea.domain.member.service.command.MemberCommandService;
import com.example.SeaTea.domain.member.service.query.MemberQueryService;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.code.BaseCode;
import com.example.SeaTea.global.exception.GeneralException;
import com.example.SeaTea.global.exception.GlobalExceptionHandler;
import com.example.SeaTea.global.status.ErrorStatus;
import com.example.SeaTea.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
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
      @RequestBody MemberReqDTO.JoinDTO dto
  ) {
    return ApiResponse.of(MemberSuccessCode.FOUND, memberCommandService.signup(dto));
  }

  // 테스트
  @GetMapping("/test")
  public ApiResponse<MemberResDTO.Tasting> test() throws Exception {
    // 응답 코드 정의
    SuccessStatus code = SuccessStatus._OK;
//    throw new MemberException(ErrorStatus._INTERNAL_SERVER_ERROR);
    return ApiResponse.onSuccess(
        MemberConverter.toTestingDTO("This is TEST!")
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
