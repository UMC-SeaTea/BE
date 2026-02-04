package com.example.SeaTea.domain.diagnosis.exception;

import com.example.SeaTea.global.code.BaseErrorCode;
import com.example.SeaTea.global.exception.GeneralException;

public class DiagnosisException extends GeneralException {
    public DiagnosisException(BaseErrorCode code) {
        super(code);
    }
}
