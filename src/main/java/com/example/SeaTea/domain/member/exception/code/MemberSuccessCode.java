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

  _FOUND(HttpStatus.OK, "MEMBER200", "성공적으로 사용자를 조회했습니다."),
  _LOGIN_SUCCESS(HttpStatus.OK, "MEMBER201", "성공적으로 로그인하였습니다."),
  _CREATED(HttpStatus.OK, "MEMBER202", "회원가입에 성공하였습니다.")
  ;

  private HttpStatus httpStatus;
  private String code;
  private String message;

  @Override
  public ReasonDTO getReason() {
    return ReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(true)
        .build();
  }

  @Override
  public ReasonDTO getReasonHttpStatus() {
    return ReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(true)
        .httpStatus(httpStatus)
        .build();
  }
}
