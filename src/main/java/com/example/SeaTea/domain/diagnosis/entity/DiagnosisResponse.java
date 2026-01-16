package com.example.SeaTea.domain.diagnosis.entity;

import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diagnosis_response")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DiagnosisResponse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어떤 세션에 대한 응답인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private DiagnosisSession session;

    //상세진단 또는 키워드의 일련번호 ex) Q01 or KW01
    @Column(name = "item_code", nullable = false)
    private String itemCode;

    //질문에 대한 사용자의 응답 ex) A, B, C / true / 100 등
    @Column(name = "answer_code", nullable = false)
    private String answerCode;
}
