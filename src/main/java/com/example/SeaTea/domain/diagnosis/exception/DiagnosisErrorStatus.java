package com.example.SeaTea.domain.diagnosis.exception;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DiagnosisErrorStatus implements BaseErrorCode {

    // 400
    _INVALID_STEP(HttpStatus.BAD_REQUEST, "DIAGNOSIS4001", "잘못된 진단 단계입니다."),
    _SESSION_ID_REQUIRED(HttpStatus.BAD_REQUEST, "DIAGNOSIS4002", "Step2 요청에는 sessionId가 필요합니다."),
    _INVALID_KEYWORDS(HttpStatus.BAD_REQUEST, "DIAGNOSIS4003", "키워드는 정확히 3개를 선택해야 합니다."),
    _STEP1_DATA_CORRUPTED(HttpStatus.BAD_REQUEST, "DIAGNOSIS4004", "Step1 데이터가 손상되었거나 누락되었습니다."),

    // 404
    _SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "DIAGNOSIS4041", "진단 세션을 찾을 수 없습니다."),
    _NO_COMPLETED_DIAGNOSIS(HttpStatus.NOT_FOUND, "DIAGNOSIS4042", "완료된 진단 결과가 없습니다."),

    // 500
    _TYPE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "DIAGNOSIS5001", "결과 타입 정보를 찾을 수 없습니다.");
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
                .build();
    }
}