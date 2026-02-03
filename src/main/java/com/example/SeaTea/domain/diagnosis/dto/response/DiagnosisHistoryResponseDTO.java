package com.example.SeaTea.domain.diagnosis.dto.response;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.diagnosis.enums.Mode;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


 // 나의 진단 히스토리 조회 응답 DTO
 // 마이페이지 > 과거 진단 결과 리스트용
@Getter
@Builder
@AllArgsConstructor
public class DiagnosisHistoryResponseDTO {

    private Long sessionId;            // 진단 세션 ID
    private Mode mode;                 // QUICK / DETAIL
    private LocalDateTime createdAt;   // 진단 완료 시각

    private String typeCode;           // ex) FLORAL
    private String displayName;        // ex) Floral
    private String imageUrl;           // 결과 이미지 URL


    //DiagnosisSession 엔티티 → 히스토리 응답 DTO 변환
    public static DiagnosisHistoryResponseDTO from(DiagnosisSession session) {
        TastingNoteType type = session.getType(); // type != null (조회 조건으로 보장)

        return DiagnosisHistoryResponseDTO.builder()
                .sessionId(session.getId())
                .mode(session.getMode())
                .createdAt(session.getCreatedAt())
                .typeCode(type.getCode())
                .displayName(type.getDisplayName())
                .imageUrl(type.getImageUrl())
                .build();
    }
}
