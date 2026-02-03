package com.example.SeaTea.domain.diagnosis.dto.request;

import com.example.SeaTea.domain.diagnosis.enums.QuickKeyword;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 간단 진단(Quick) 요청 DTO
 * - 프론트에서 선택 순서대로 3개의 키워드를 배열로 전달 (0번째=1순위)
 */
@NoArgsConstructor
@Getter
public class DiagnosisQuickRequestDTO {

    @NotNull(message = "keywords는 필수입니다.") //리스트 자체가 null인지 확인하는거지, 요소는 검증x
    @Size(min = 3, max = 3, message = "keywords는 정확히 3개를 선택해야 합니다.")
    private List<@NotNull QuickKeyword> keywords; //각 요소도 null 불가

}
