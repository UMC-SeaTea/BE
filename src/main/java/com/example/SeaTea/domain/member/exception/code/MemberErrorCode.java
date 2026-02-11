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
  _NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER400", "사용자가 존재하지 않습니다"),
  _LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "MEMBER401", "로그인에 실패했습니다(인증 실패 / 아이디 비번 불일치)"),
  _CONFLICT_EMAIL(HttpStatus.CONFLICT, "MEMBER402", "중복되는 이메일입니다."),
  _CONFLICT_NICKNAME(HttpStatus.CONFLICT, "MEMBER403", "중복되는 닉네임입니다."),
  _DIFFERENT_PW(HttpStatus.BAD_REQUEST, "MEMBER404", "비밀번호와 비밀번호 확인이 다릅니다."),
  _NOT_LOGIN(HttpStatus.UNAUTHORIZED, "MEMBER405", "로그인되지 않은 상태입니다."),
  _FILE_EMPTY(HttpStatus.BAD_REQUEST, "MEMBER406", "파일이 비어있습니다."),
  _JWT_WRONG(HttpStatus.UNAUTHORIZED, "MEMBER407", "JWT 토큰에 오류가 발생하였습니다."),
  _NOT_RIGHT(HttpStatus.FORBIDDEN, "MEMBER408", "토큰에 권한 정보가 없습니다.")

  // 일반 로그인 및 카카오 로그인 이외 로그인 시도
  // _INVALID_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "MEMBER406", "지원하지 않는 로그인 형식입니다."),
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
