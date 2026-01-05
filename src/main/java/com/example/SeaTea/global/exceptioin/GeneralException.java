package com.example.SeaTea.global.exceptioin;

import com.example.SeaTea.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
  private final BaseErrorCode code;
}
