package com.example.SeaTea.global.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
  ReasonDTO getReason();
  ReasonDTO getReasonHttpStatus();
}
