package com.example.SeaTea.domain.member.exception.code;

import com.example.SeaTea.global.code.BaseCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import com.example.SeaTea.global.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseCode {

  FOUND(HttpStatus.OK,
      "MEMBER200_1",
      "성공적으로 사용자를 조회했습니다."),
  LOGIN_SUCCESS(HttpStatus.OK,
      "MEMBER200_1",
      "성공적으로 로그인하였습니다.")
  ;

  private HttpStatus httpStatus;
  private String message;
  private String code;

  @Override
  public ReasonDTO getReason() {
    return ReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .build();
  }

  @Override
  public ReasonDTO getReasonHttpStatus() {
    return ReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .httpStatus(httpStatus)
        .build();
  }
}
