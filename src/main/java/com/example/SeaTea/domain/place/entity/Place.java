package com.example.SeaTea.domain.place.entity;

import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    // FK 컬럼을 읽기 전용으로
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tasting_type_id", insertable = false, updatable = false)
    private TastingNoteType tastingType;

    @Column(name = "tasting_type_id")
    private Long tastingTypeId;

    @Column(name = "naver_place_id", unique = true)
    private Long naverPlaceId;

    @Column(name = "name")
    private String name;

    @Column(name = "thumbnail_image_url", length = 255)
    private String thumbnailImageUrl;

    @Column(name = "phone")
    private String phone;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "address")
    private String address;

    @Column(name = "lng", precision = 11, scale = 7)
    private BigDecimal lng;

    @Column(name = "lat", precision = 11, scale = 7)
    private BigDecimal lat;

    @Column(name = "opening_hours", columnDefinition = "TEXT")
    private String openingHours;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
