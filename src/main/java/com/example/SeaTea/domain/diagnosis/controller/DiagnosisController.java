package com.example.SeaTea.domain.diagnosis.controller;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisSubmitResponseDTO;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {
    @PostMapping("/detail")
    public ApiResponse<DiagnosisSubmitResponseDTO> submitDetailDiagnosis(
            @RequestBody @Valid DiagnosisSubmitRequestDTO request
    ) {
        // TODO: service 호출
        return ApiResponse.onSuccess(null);
    }
}
