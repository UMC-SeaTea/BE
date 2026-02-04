package com.example.SeaTea.domain.diagnosis.exception;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DiagnosisErrorStatus implements BaseErrorCode {

    _NO_COMPLETED_DIAGNOSIS(
            HttpStatus.NOT_FOUND,
            "DIAGNOSIS404",
            "완료된 진단 결과가 없습니다."
    ),

    _INVALID_STEP(
            HttpStatus.BAD_REQUEST,
            "DIAGNOSIS400",
            "잘못된 진단 단계입니다."
    ),

    _SESSION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "DIAGNOSIS404_1",
            "진단 세션을 찾을 수 없습니다."
    ),

    _TYPE_NOT_FOUND(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "DIAGNOSIS500_1",
            "결과 타입 정보를 찾을 수 없습니다."
    );

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