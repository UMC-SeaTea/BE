package com.example.SeaTea.domain.member.exception;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.exception.GeneralException;

public class MemberException extends GeneralException {
  private MemberException(BaseErrorCode code){
    super(code);
  }
}
