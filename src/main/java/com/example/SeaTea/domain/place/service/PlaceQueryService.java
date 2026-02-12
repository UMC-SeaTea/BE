package com.example.SeaTea.domain.place.service;

import com.example.SeaTea.domain.place.dto.SpaceBoundsResponse;
import com.example.SeaTea.domain.place.dto.SpaceBoundsResponse.SpaceBoundsItem;
import com.example.SeaTea.domain.place.dto.SpaceDetailResponse;
import com.example.SeaTea.domain.place.dto.SpaceListResponse;
import com.example.SeaTea.domain.place.dto.SpaceListResponse.CursorInfo;
import com.example.SeaTea.domain.place.dto.SpaceListResponse.SpaceItem;
import com.example.SeaTea.domain.place.dto.SpaceListResponse.SpaceItemWithDescription;
import com.example.SeaTea.domain.place.entity.MemberRecentPlace;
import com.example.SeaTea.domain.place.entity.MemberSavedPlace;
import com.example.SeaTea.domain.place.entity.Place;
import com.example.SeaTea.domain.place.repository.MemberRecentPlaceRepository;
import com.example.SeaTea.domain.place.repository.MemberSavedPlaceRepository;
import com.example.SeaTea.domain.place.repository.PlaceRepository;
import com.example.SeaTea.domain.place.repository.PlaceRepository.PlaceDistanceView;
import com.example.SeaTea.domain.place.status.SpaceErrorStatus;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.global.exception.GeneralException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceQueryService {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DISTANCE_SORT = "distance,asc";
    private static final String ID_SORT = "id,asc";
    private static final String RECENT_SORT = "recent";

    private final PlaceRepository placeRepository;
    private final MemberSavedPlaceRepository memberSavedPlaceRepository;
    private final MemberRecentPlaceRepository memberRecentPlaceRepository;
    private final Environment environment;

    public SpaceListResponse getSpaces(Double lat,
                                       Double lng,
                                       String q,
                                       Integer size,
                                       String cursor) {
        String keyword = normalizeKeyword(q);
        int pageSize = normalizeSize(size);

        // 커서 decode 해서 keyset 기준으로 사용
        SpaceCursor cursorToken = null;
        if (cursor != null) {
            try {
                cursorToken = SpaceCursor.decode(cursor);
            } catch (IllegalArgumentException e) {
                throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
            }
        }

        // 위치 정보가 있으면 기반 거리 정렬
        boolean hasLocation = lat != null || lng != null;
        if (hasLocation) {
            validateLocation(lat, lng);
            if (cursorToken != null && cursorToken.getSort() != null
                    && !DISTANCE_SORT.equalsIgnoreCase(cursorToken.getSort())) {
                throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
            }
            return fetchByDistance(lat, lng, keyword, pageSize, cursorToken);
        }

        if (cursorToken != null && cursorToken.getSort() != null
                && !ID_SORT.equalsIgnoreCase(cursorToken.getSort())) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        return fetchById(keyword, pageSize, cursorToken);
    }

    // 타입별 공간 목록 조회 (관리/검수/탐색 용도)
    // - tastingTypeCode 기준으로 공간을 조회합니다.
    // - size/cursor 기반 커서 페이징을 지원합니다.
    // - lat/lng가 전달되면 distanceMeters를 계산하여 응답에 포함합니다. (정렬에는 사용하지 않음)
    public SpaceListResponse getRecommendedSpaces(
            String tastingTypeCode,
            Double lat,
            Double lng,
            Integer size,
            String cursor
    ) {
        // 1) 휴식 유형 코드 정규화 및 유효성 검증 (Enum 기준)
        String normalizedTypeCode = normalizeAndValidateTypeCode(tastingTypeCode);

        // 2) 사이즈 정규화
        int pageSize = normalizeSize(size);

        // 3) 커서 디코딩 (id 기반 keyset pagination)
        SpaceCursor cursorToken = null;
        if (cursor != null) {
            try {
                cursorToken = SpaceCursor.decode(cursor);
            } catch (IllegalArgumentException e) {
                throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
            }
        }
        if (cursorToken != null && cursorToken.getSort() != null
                && !ID_SORT.equalsIgnoreCase(cursorToken.getSort())) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        Long lastId = cursorToken == null ? null : cursorToken.getLastId();

        // 4) 위치값 검증 + 거리 계산 여부 판단 (표시용)
        boolean hasLocation = lat != null || lng != null;
        if (hasLocation) {
            validateLocation(lat, lng);
        }

        // 5) 다음 페이지 여부 판단을 위해 size + 1 만큼 조회
        int limit = pageSize + 1;
        List<PlaceDistanceView> rows = placeRepository.findByTastingTypeWithCursor(
                normalizedTypeCode,
                lastId,
                limit
        );

        boolean hasNext = rows.size() > pageSize;
        List<PlaceDistanceView> page = hasNext ? rows.subList(0, pageSize) : rows;

        List<SpaceItemWithDescription> items = new ArrayList<>();
        for (PlaceDistanceView row : page) {
            Long distanceMeters = null;
            if (hasLocation && row.getLat() != null && row.getLng() != null) {
                distanceMeters = Math.round(
                        haversineMeters(lat, lng, row.getLat().doubleValue(), row.getLng().doubleValue())
                );
            }
            String description = row.getDescription() != null && !row.getDescription().isBlank()
                    ? row.getDescription()
                    : row.getNote();
            items.add(new SpaceItemWithDescription(
                    row.getPlaceId(),
                    row.getName(),
                    row.getTastingTypeCode(),
                    toDouble(row.getLat()),
                    toDouble(row.getLng()),
                    row.getThumbnailImageUrl(),
                    row.getAddress(),
                    description,
                    distanceMeters
            ));
        }

        String nextCursor = null;
        if (hasNext) {
            PlaceDistanceView last = page.get(page.size() - 1);
            SpaceCursor next = new SpaceCursor(last.getPlaceId(), ID_SORT, null);
            nextCursor = SpaceCursor.encode(next);
        }

        return new SpaceListResponse(items, new CursorInfo(nextCursor, hasNext));
    }

    private static final String TEABAG_SORT_LATEST = "latest";
    private static final String TEABAG_SORT_SAVED = "saved";

    public SpaceListResponse getMyTeabagSpaces(Member member, String sort, Integer size, String cursor) {
        int pageSize = normalizeSize(size);
        String normalizedSort = normalizeTeabagSort(sort);

        Long lastId = null;
        if (cursor != null) {
            try {
                SpaceCursor cursorToken = SpaceCursor.decode(cursor);
                lastId = cursorToken.getLastId();
                String cursorSort = cursorToken.getSort();
                if (cursorSort == null || !cursorSort.equalsIgnoreCase(normalizedSort)) {
                    throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
                }
            } catch (IllegalArgumentException e) {
                throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
            }
        }

        Pageable pageable = PageRequest.of(0, pageSize + 1);
        List<MemberSavedPlace> rows = TEABAG_SORT_LATEST.equals(normalizedSort)
                ? memberSavedPlaceRepository.findByMemberIdWithCursorDesc(member.getId(), lastId, pageable)
                : memberSavedPlaceRepository.findByMemberIdWithCursorAsc(member.getId(), lastId, pageable);

        boolean hasNext = rows.size() > pageSize;
        List<MemberSavedPlace> page = hasNext ? rows.subList(0, pageSize) : rows;

        List<SpaceItem> items = new ArrayList<>();
        for (MemberSavedPlace msp : page) {
            Place place = msp.getPlace();
            String tastingTypeCode = place.getTastingType() == null
                    ? null
                    : place.getTastingType().getCode();
            items.add(new SpaceItem(
                    place.getPlaceId(),
                    place.getName(),
                    tastingTypeCode,
                    toDouble(place.getLat()),
                    toDouble(place.getLng()),
                    place.getThumbnailImageUrl(),
                    place.getAddress(),
                    null
            ));
        }

        String nextCursor = null;
        if (hasNext) {
            MemberSavedPlace last = page.get(page.size() - 1);
            nextCursor = SpaceCursor.encode(new SpaceCursor(last.getMemberSavedPlaceId(), normalizedSort, null));
        }

        return new SpaceListResponse(items, new CursorInfo(nextCursor, hasNext));
    }

    private String normalizeTeabagSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return TEABAG_SORT_LATEST;
        }
        String normalized = sort.trim().toLowerCase();
        if (TEABAG_SORT_LATEST.equals(normalized) || TEABAG_SORT_SAVED.equals(normalized)) {
            return normalized;
        }
        throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
    }

    public SpaceListResponse getRecentPlaces(Member member, Integer size, String cursor) {
        int pageSize = normalizeSize(size);

        LocalDateTime lastViewedAt = null;
        Long lastId = null;
        if (cursor != null) {
            try {
                SpaceCursor cursorToken = SpaceCursor.decode(cursor);
                lastId = cursorToken.getLastId();
                if (cursorToken.getSort() == null || !RECENT_SORT.equalsIgnoreCase(cursorToken.getSort())) {
                    throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
                }
                if (cursorToken.getLastDistance() != null) {
                    lastViewedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(cursorToken.getLastDistance().longValue()),
                        ZoneId.systemDefault()
                    );
                }
            } catch (IllegalArgumentException e) {
                throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
            }
        }

        Pageable pageable = PageRequest.of(0, pageSize + 1);
        List<MemberRecentPlace> rows = memberRecentPlaceRepository.findByMemberIdWithCursor(
                member.getId(), lastViewedAt, lastId, pageable);

        boolean hasNext = rows.size() > pageSize;
        List<MemberRecentPlace> page = hasNext ? rows.subList(0, pageSize) : rows;

        List<SpaceItem> items = new ArrayList<>();
        for (MemberRecentPlace mrp : page) {
            Place place = mrp.getPlace();
            String tastingTypeCode = place.getTastingType() == null
                    ? null
                    : place.getTastingType().getCode();
            items.add(new SpaceItem(
                    place.getPlaceId(),
                    place.getName(),
                    tastingTypeCode,
                    toDouble(place.getLat()),
                    toDouble(place.getLng()),
                    place.getThumbnailImageUrl(),
                    place.getAddress(),
                    null
            ));
        }

        String nextCursor = null;
        if (hasNext) {
            MemberRecentPlace last = page.get(page.size() - 1);
            double viewedAtEpoch = last.getViewedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            nextCursor = SpaceCursor.encode(new SpaceCursor(last.getMemberRecentPlaceId(), RECENT_SORT, viewedAtEpoch));
        }

        return new SpaceListResponse(items, new CursorInfo(nextCursor, hasNext));
    }

    public SpaceDetailResponse getSpaceDetail(Long spaceId,
                                              Double lat,
                                              Double lng,
                                              Member member) {
        if (lat != null || lng != null) {
            validateLocation(lat, lng);
        }

        Place place = placeRepository.findById(spaceId)
                .orElseThrow(() -> new GeneralException(SpaceErrorStatus._NOT_FOUND));

        Long savedCount = memberSavedPlaceRepository.countByPlace_PlaceId(spaceId);

        Boolean isSaved = null;
        Long sameTypeSavedCount = null;
        if (member != null) {
            isSaved = memberSavedPlaceRepository.existsByMember_IdAndPlace_PlaceId(member.getId(), spaceId);
            Long tastingTypeId = place.getTastingTypeId();
            if (tastingTypeId != null) {
                sameTypeSavedCount = memberSavedPlaceRepository.countByMemberIdAndTastingTypeId(
                        member.getId(), tastingTypeId
                );
            }
        }

        Long distanceMeters = null;
        if (lat != null && lng != null && place.getLat() != null && place.getLng() != null) {
            distanceMeters = Math.round(
                    haversineMeters(lat, lng, place.getLat().doubleValue(), place.getLng().doubleValue())
            );
        }

        String tastingTypeCode = place.getTastingType() == null
                ? null
                : place.getTastingType().getCode();

        return new SpaceDetailResponse(
                place.getPlaceId(),
                place.getName(),
                tastingTypeCode,
                toDouble(place.getLat()),
                toDouble(place.getLng()),
                place.getThumbnailImageUrl(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getPhone(),
                place.getOpeningHours(),
                place.getDescription(),
                place.getNote(),
                distanceMeters,
                savedCount,
                sameTypeSavedCount,
                isSaved
        );
    }

    public SpaceBoundsResponse getSpacesByBounds(Double southWestLat,
                                                 Double southWestLng,
                                                 Double northEastLat,
                                                 Double northEastLng) {
        validateBounds(southWestLat, southWestLng, northEastLat, northEastLng);

        List<Place> places = placeRepository.findByBounds(
            BigDecimal.valueOf(southWestLat),
            BigDecimal.valueOf(southWestLng),
            BigDecimal.valueOf(northEastLat),
            BigDecimal.valueOf(northEastLng)
        );

        List<SpaceBoundsItem> items = new ArrayList<>();
        for (Place place : places) {
            items.add(new SpaceBoundsItem(
                place.getPlaceId(),
                place.getName(),
                place.getTastingType() == null ? null : place.getTastingType().getCode(),
                toDouble(place.getLat()),
                toDouble(place.getLng())
            ));
        }

        return new SpaceBoundsResponse(items);
    }

    private SpaceListResponse fetchByDistance(double lat,
                                              double lng,
                                              String keyword,
                                              int size,
                                              SpaceCursor cursorToken) {
        Double lastDistance = cursorToken == null ? null : cursorToken.getLastDistance();
        Long lastId = cursorToken == null ? null : cursorToken.getLastId();

        // 다음 페이지 여부 판별을 위해 size + 1 조회
        List<PlaceDistanceView> rows = isH2()
                ? placeRepository.findByDistanceWithCursorH2(lat, lng, keyword, lastDistance, lastId, size + 1)
                : placeRepository.findByDistanceWithCursor(lat, lng, keyword, lastDistance, lastId, size + 1);

        boolean hasNext = rows.size() > size;
        List<PlaceDistanceView> page = hasNext ? rows.subList(0, size) : rows;

        List<SpaceListResponse.SpaceItem> items = new ArrayList<>();
        for (PlaceDistanceView row : page) {
            items.add(new SpaceListResponse.SpaceItem(
                    row.getPlaceId(),
                    row.getName(),
                    row.getTastingTypeCode(),
                    toDouble(row.getLat()),
                    toDouble(row.getLng()),
                    row.getThumbnailImageUrl(),
                    row.getAddress(),
                    row.getDistanceMeters() == null ? null : Math.round(row.getDistanceMeters())
            ));
        }

        String nextCursor = null;
        if (hasNext) {
            PlaceDistanceView last = page.get(page.size() - 1);
            SpaceCursor next = new SpaceCursor(
                    last.getPlaceId(),
                    DISTANCE_SORT,
                    last.getDistanceMeters()
            );
            nextCursor = SpaceCursor.encode(next);
        }

        return new SpaceListResponse(items, new CursorInfo(nextCursor, hasNext));
    }

    private SpaceListResponse fetchById(String keyword, int size, SpaceCursor cursorToken) {
        Long lastId = cursorToken == null ? null : cursorToken.getLastId();
        // 다음 페이지 여부 판별을 위해 size + 1 조회
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Place> rows = placeRepository.findByNameWithCursor(keyword, lastId, pageable);

        boolean hasNext = rows.size() > size;
        List<Place> page = hasNext ? rows.subList(0, size) : rows;

        List<SpaceListResponse.SpaceItem> items = new ArrayList<>();
        for (Place place : page) {
            items.add(new SpaceListResponse.SpaceItem(
                    place.getPlaceId(),
                    place.getName(),
                    place.getTastingType() == null ? null : place.getTastingType().getCode(),
                    toDouble(place.getLat()),
                    toDouble(place.getLng()),
                    place.getThumbnailImageUrl(),
                    place.getAddress(),
                    null
            ));
        }

        String nextCursor = null;
        if (hasNext) {
            Place last = page.get(page.size() - 1);
            SpaceCursor next = new SpaceCursor(last.getPlaceId(), ID_SORT, null);
            nextCursor = SpaceCursor.encode(next);
        }

        return new SpaceListResponse(items, new CursorInfo(nextCursor, hasNext));
    }

    // lat/lng 범위 확인
    private void validateLocation(Double lat, Double lng) {
        if (lat == null || lng == null) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        if (!Double.isFinite(lat) || !Double.isFinite(lng)) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        return size;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private void validateBounds(Double southWestLat,
                                Double southWestLng,
                                Double northEastLat,
                                Double northEastLng) {
        if (southWestLat == null || southWestLng == null || northEastLat == null || northEastLng == null) {
            throw new GeneralException(SpaceErrorStatus._INVALID_BOUNDS);
        }
        if (!Double.isFinite(southWestLat) || !Double.isFinite(southWestLng)
            || !Double.isFinite(northEastLat) || !Double.isFinite(northEastLng)) {
            throw new GeneralException(SpaceErrorStatus._INVALID_BOUNDS);
        }
        if (southWestLat < -90 || southWestLat > 90 || northEastLat < -90 || northEastLat > 90
            || southWestLng < -180 || southWestLng > 180 || northEastLng < -180 || northEastLng > 180) {
            throw new GeneralException(SpaceErrorStatus._INVALID_BOUNDS);
        }
        if (southWestLat > northEastLat || southWestLng > northEastLng) {
            throw new GeneralException(SpaceErrorStatus._INVALID_BOUNDS);
        }
    }

    private double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.pow(Math.sin(dLng / 2), 2);
        // 부동소수점 오차로 a가 1을 초과하는 경우 방어 처리
        double clamped = Math.min(1.0d, a);
        return 2 * 6371000 * Math.asin(Math.sqrt(clamped));
    }

    private boolean isH2() {
        String url = environment.getProperty("spring.datasource.url");
        return url != null && url.startsWith("jdbc:h2");
    }

    // 휴식 유형 코드 정규화 및 검증
    // - null/blank 방어
    // - 대문자 변환 후 TastingNoteTypeCode enum 기준으로 검증
    private String normalizeAndValidateTypeCode(String tastingTypeCode) {
        if (tastingTypeCode == null || tastingTypeCode.trim().isEmpty()) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        String normalizedTypeCode = tastingTypeCode.trim().toUpperCase();
        try {
            com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode.valueOf(normalizedTypeCode);
        } catch (IllegalArgumentException e) {
            throw new GeneralException(SpaceErrorStatus._INVALID_PARAMS);
        }
        return normalizedTypeCode;
    }

    // 타입 내 랜덤 공간 추천 (PM 요구사항)
    // - 해당 휴식 유형에 속한 공간 중 최대 3개를 랜덤으로 반환
    // - lat/lng가 전달되면 distanceMeters를 계산하여 응답에 포함
    public SpaceListResponse recommend3SpacesRandom(
            String tastingTypeCode,
            Double lat,
            Double lng
    ) {
        // 1) 휴식 유형 코드 정규화 및 유효성 검증
        String normalizedTypeCode = normalizeAndValidateTypeCode(tastingTypeCode);

        // 2) 위치값 검증 (선택 파라미터)
        boolean hasLocation = lat != null || lng != null;
        if (hasLocation) {
            validateLocation(lat, lng);
        }

        // 3) 타입에 해당하는 공간을 충분히 많이 조회 (현재 규모 기준으로 500은 안전)
        List<PlaceDistanceView> rows = placeRepository.findByTastingTypeWithCursor(
                normalizedTypeCode,
                null,
                500
        );

        // 4) 추천 노출을 위한 랜덤 섞기
        java.util.Collections.shuffle(rows);

        // 5) 최대 3개까지만 추출
        int take = Math.min(3, rows.size());
        List<PlaceDistanceView> page = rows.subList(0, take);

        List<SpaceItemWithDescription> items = new ArrayList<>();
        for (PlaceDistanceView row : page) {
            Long distanceMeters = null;
            if (hasLocation && row.getLat() != null && row.getLng() != null) {
                distanceMeters = Math.round(
                        haversineMeters(lat, lng, row.getLat().doubleValue(), row.getLng().doubleValue())
                );
            }
            String description = row.getDescription() != null && !row.getDescription().isBlank()
                    ? row.getDescription()
                    : row.getNote();
            items.add(new SpaceItemWithDescription(
                    row.getPlaceId(),
                    row.getName(),
                    row.getTastingTypeCode(),
                    toDouble(row.getLat()),
                    toDouble(row.getLng()),
                    row.getThumbnailImageUrl(),
                    row.getAddress(),
                    description,
                    distanceMeters
            ));
        }

        // 6) 추천 API는 페이징/커서 정보를 제공하지 않음
        return new SpaceListResponse(items, new CursorInfo(null, false));
    }
}