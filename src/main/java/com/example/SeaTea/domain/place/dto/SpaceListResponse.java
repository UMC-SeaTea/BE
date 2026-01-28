package com.example.SeaTea.domain.place.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpaceListResponse {
    private final List<SpaceItem> items;
    private final CursorInfo cursorInfo;

    @Getter
    @AllArgsConstructor
    public static class SpaceItem {
        private final Long spaceId;
        private final String name;
        private final String tastingTypeCode;
        private final Double lat;
        private final Double lng;
        private final String thumbnailImageUrl;
        private final String address;
        private final Long distanceMeters;
    }

    @Getter
    @AllArgsConstructor
    public static class CursorInfo {
        private final String nextCursor;
        private final boolean hasNext;
    }
}
