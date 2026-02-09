package com.example.SeaTea.domain.place.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpaceBoundsResponse {
    private final List<SpaceBoundsItem> items;

    @Getter
    @AllArgsConstructor
    public static class SpaceBoundsItem {
        private final Long spaceId;
        private final String name;
        private final String tastingTypeCode;
        private final Double lat;
        private final Double lng;
    }
}
