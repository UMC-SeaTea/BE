package com.example.SeaTea.global.exception;

import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.code.ErrorReasonDTO;
import com.example.SeaTea.global.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 1. 비즈니스 로직 예외 처리 (Custom Exception)
   */
  @ExceptionHandler(GeneralException.class)
  public ResponseEntity<ApiResponse<Object>> handleGeneralException(GeneralException e) {
    // 에러 코드에서 상태와 메시지 추출
    BaseErrorCode code = e.getCode();
    ErrorReasonDTO reason = code.getReasonHttpStatus();

    log.warn("[GeneralException] Code: {}, Message: {}", reason.getCode(), reason.getMessage());

    return ResponseEntity
        .status(reason.getHttpStatus())
        .body(ApiResponse.onFailure(
            reason.getCode(),
            reason.getMessage(),
            null // 별도 데이터 없음
        ));
  }

  /**
   * 2. @Valid 유효성 검사 실패 (RequestBody)
   * DTO 검증 실패 시 발생합니다.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e, HttpServletRequest request) {

    log.warn("[MethodArgumentNotValid] Url: {}, Message: {}", request.getRequestURI(), e.getMessage());

    // 에러 필드와 메시지를 Map으로 변환
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors().forEach(fieldError -> {
      String fieldName = fieldError.getField();
      String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
      errors.merge(fieldName, errorMessage, (existing, newMsg) -> existing + ", " + newMsg);
    });

    // ErrorStatus._BAD_REQUEST 활용
    ErrorReasonDTO reason = ErrorStatus._BAD_REQUEST.getReasonHttpStatus();

    return ResponseEntity
        .status(reason.getHttpStatus())
        .body(ApiResponse.onFailure(
            reason.getCode(),
            reason.getMessage(),
            errors // 실패한 필드 정보를 result(data)에 담아 전달
        ));
  }

  /**
   * 3. @Validated 유효성 검사 실패 (RequestParam, PathVariable)
   * 컨트롤러 메서드 파라미터 검증 실패 시 발생합니다.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
      ConstraintViolationException e, HttpServletRequest request) {

    log.warn("[ConstraintViolation] Url: {}, Message: {}", request.getRequestURI(), e.getMessage());

    // 에러 메시지 추출 (여러 개일 경우 첫 번째 것만 혹은 조합)
    String errorMessage = e.getConstraintViolations().stream()
        .map(ConstraintViolation::getMessage)
        .findFirst()
        .orElse("Invalid input");

    ErrorReasonDTO reason = ErrorStatus._BAD_REQUEST.getReasonHttpStatus();

    return ResponseEntity
        .status(reason.getHttpStatus())
        .body(ApiResponse.onFailure(
            reason.getCode(),
            reason.getMessage(),
            errorMessage // 구체적인 에러 사유를 데이터로 전달
        ));
  }

  /**
   * 4. 기타 서버 내부 에러
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
    log.error("[Exception] Url: {}, Message: {}", request.getRequestURI(), e.getMessage(), e);

    ErrorReasonDTO reason = ErrorStatus._INTERNAL_SERVER_ERROR.getReasonHttpStatus();

    return ResponseEntity
        .status(reason.getHttpStatus())
        .body(ApiResponse.onFailure(
            reason.getCode(),
            reason.getMessage(),
            e.getMessage() // 디버깅용 메시지 (배포 시에는 null로 처리 권장)
        ));
  }
}
