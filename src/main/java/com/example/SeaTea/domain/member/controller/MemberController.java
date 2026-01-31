package com.example.SeaTea.domain.member.controller;

import com.example.SeaTea.domain.member.converter.MemberConverter;
import com.example.SeaTea.domain.member.dto.response.MemberResDTO;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.code.BaseCode;
import com.example.SeaTea.global.exception.GeneralException;
import com.example.SeaTea.global.exception.GlobalExceptionHandler;
import com.example.SeaTea.global.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 테스트
@RestController
@RequiredArgsConstructor
@RequestMapping("/temp")
public class MemberController {

  @GetMapping("/test")
  public ApiResponse<MemberResDTO.Tasting> test() throws Exception {
    // 응답 코드 정의
    SuccessStatus code = SuccessStatus._OK;
    return ApiResponse.onSuccess(
        MemberConverter.toTestingDTO("This is TEST!")
    );
  }

}
