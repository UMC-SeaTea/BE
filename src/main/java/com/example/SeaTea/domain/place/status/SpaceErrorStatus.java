package com.example.SeaTea.domain.place.status;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SpaceErrorStatus implements BaseErrorCode {
    // 위치 정보(lat,lng) 누락/범위 초과/NaN
    _INVALID_PARAMS(
            HttpStatus.BAD_REQUEST,
            "PLACE400",
            "위치 정보(lat,lng)가 올바르지 않습니다."
    ),

    // 지도 영역 좌표 누락/범위 초과/southWest-northEast 관계 오류
    _INVALID_BOUNDS(
            HttpStatus.BAD_REQUEST,
            "PLACE400",
            "지도 영역 좌표가 올바르지 않습니다."
    ),

    // spaceId에 해당하는 공간 없음
    _NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLACE404",
            "공간을 찾을 수 없습니다."
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
