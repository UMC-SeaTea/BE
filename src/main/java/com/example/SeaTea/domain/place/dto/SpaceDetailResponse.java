package com.example.SeaTea.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpaceDetailResponse {
    private final Long spaceId;
    private final String name;
    private final String tastingTypeCode;
    private final Double lat;
    private final Double lng;
    private final String thumbnailImageUrl;
    private final String address;
    private final String roadAddress;
    private final String phone;
    private final String openingHours;
    private final String description;
    private final String note;
    private final Long distanceMeters;
    private final Long savedCount;
    private final Long sameTypeSavedCount;
    private final Boolean isSaved;
}
