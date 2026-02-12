package com.example.SeaTea.domain.place.status;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SpaceErrorStatus implements BaseErrorCode {
    // size 파라미터 범위/값 오류
    _INVALID_SIZE(
            HttpStatus.BAD_REQUEST,
            "PLACE400",
            "size 파라미터가 올바르지 않습니다. (1~100 사이의 정수여야 합니다.)"
    ),

    // cursor 토큰 포맷/정렬 기준 불일치
    _INVALID_CURSOR(
            HttpStatus.BAD_REQUEST,
            "PLACE400",
            "cursor 토큰이 올바르지 않습니다."
    ),

    // sort 값이 허용된 값(latest, saved 등)이 아님
    _INVALID_SORT(
            HttpStatus.BAD_REQUEST,
            "PLACE400",
            "정렬 기준(sort) 값이 올바르지 않습니다."
    ),

    // tastingTypeCode 등 휴식유형 코드가 Enum 에 없음
    _INVALID_TASTING_TYPE(
            HttpStatus.BAD_REQUEST,
            "PLACE400",
            "휴식유형 코드(tastingTypeCode)가 올바르지 않습니다."
    ),

    // 위치 정보(lat,lng) 누락/범위 초과/NaN
    _INVALID_LOCATION(
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
