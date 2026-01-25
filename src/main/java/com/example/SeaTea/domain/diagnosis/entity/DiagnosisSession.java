package com.example.SeaTea.domain.diagnosis.entity;

import com.example.SeaTea.domain.diagnosis.enums.Mode;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "diagnosis_session")
public class DiagnosisSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //한 사용자가 여러번 진단이 가능.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id" ,nullable = false )
    private Member member;

    //진단 결과 유형
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private TastingNoteType type;

    //진단 방법 (상세 or 간단)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mode mode;
}
