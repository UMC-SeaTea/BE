package com.example.SeaTea.domain.diagnosis.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DiagnosisSubmitRequestDTO {
    @NotNull
    private Integer step;

    private Long sessionId;

    // Q1, Q2: "A" or "B"
    private String q1;
    private String q2;

    // Q3: 0~100 (step1에서만 사용)
    @Min(0) @Max(100)
    private Integer q3;

    // Q4: 최소 1개, 최대 2개 (step1에서만 사용)
    @Size(min = 1, max = 2)
    private List<String> q4;

    // Q5~Q8: "A" or "B" (step2에서만 사용)
    private String q5;
    private String q6;
    private String q7;
    private String q8;

}
