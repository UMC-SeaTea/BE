package com.example.SeaTea.domain.diagnosis.dto.response;

import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 나의 진단 결과(최근 1건) 조회 응답 DTO
 * - 홈/마이페이지 상단에 노출되는 결과 카드용
 */
@Getter
@Builder
@AllArgsConstructor
public class DiagnosisResultResponseDTO {

    private String typeCode;      // ex) FLORAL
    private String displayName;   // ex) Floral
    private String subtitle;      // ex) 감각적인 영감의 휴식
    private String description;   // 상세 설명
    private String imageUrl;      // 결과 이미지 URL

    //TastingNoteType 엔티티 → 결과 응답 DTO 변환
    public static DiagnosisResultResponseDTO from(TastingNoteType type) {
        return DiagnosisResultResponseDTO.builder()
                .typeCode(type.getCode())
                .displayName(type.getDisplayName())
                .subtitle(type.getSubtitle())
                .description(type.getDescription())
                .imageUrl(type.getImageUrl())
                .build();
    }
}
