package com.example.SeaTea.domain.member.exception.code;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

  // for test
  NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "사용자가 존재하지 않습니다"),
  LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "MEMBER401_1", "로그인에 실패했습니다(인증 실패 / 아이디 비번 불일치)"),
  ;


  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  @Override
  public ErrorReasonDTO getReason() {
    return ErrorReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .build();
  }

  @Override
  public ErrorReasonDTO getReasonHttpStatus() {
    return ErrorReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .httpStatus(httpStatus)
        .build()
        ;
  }
}
