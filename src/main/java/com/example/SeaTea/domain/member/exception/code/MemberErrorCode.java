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
  NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER400_1", "사용자가 존재하지 않습니다"),
  ;

  private final HttpStatus httpStatus;
  private final String message;
  private final String code;

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
