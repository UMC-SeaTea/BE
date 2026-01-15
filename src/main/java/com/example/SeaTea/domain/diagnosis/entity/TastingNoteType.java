package com.example.SeaTea.domain.diagnosis.entity;

import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "tasting_note_type")
public class TastingNoteType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String code; //ex) FLORAL

    @Column(nullable = false, name = "display_name")
    private String displayName; //ex) Floral

    @Column
    private String subtitle; //ex) 감각적인 영감의 휴식

    @Column
    private String description; //ex) 혼자만의 시간을 선호하지만..

    @Column(name = "image_url")
    private String imageUrl; //ex) 전용 이미지
}
