package com.example.SeaTea.domain.place.repository;

import com.example.SeaTea.domain.place.entity.Place;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 이름 검색 + keyset
    @Query("""
        select p from Place p
        where (:q is null or p.name like concat('%', :q, '%'))
          and (:cursorId is null or p.placeId > :cursorId)
        order by p.placeId asc
        """)
    List<Place> findByNameWithCursor(@Param("q") String q,
                                     @Param("cursorId") Long cursorId,
                                     Pageable pageable);

    interface PlaceDistanceView {
        Long getPlaceId();
        String getName();
        String getTastingTypeCode();
        java.math.BigDecimal getLat();
        java.math.BigDecimal getLng();
        String getThumbnailImageUrl();
        String getAddress();
        String getDescription();
        String getNote();
        Double getDistanceMeters();
    }

    // 타입(code)으로 장소 추천/검색 + keyset (distance 없이)
    @Query(value = """
        select
            p.place_id as placeId,
            p.name as name,
            t.code as tastingTypeCode,
            p.lat as lat,
            p.lng as lng,
            p.thumbnail_image_url as thumbnailImageUrl,
            p.address as address,
            p.description as description,
            p.note as note,
            null as distanceMeters
        from place p
        left join tasting_note_type t on t.id = p.tasting_type_id
        where t.code = :tastingTypeCode
          and (:lastId is null or p.place_id > :lastId)
        order by p.place_id asc
        limit :limit
        """, nativeQuery = true)
    List<PlaceDistanceView> findByTastingTypeWithCursor(@Param("tastingTypeCode") String tastingTypeCode,
                                                       @Param("lastId") Long lastId,
                                                       @Param("limit") int limit);


    // MySQL 거리 정렬 + keyset
    @Query(value = """
        select * from (
            select
                p.place_id as placeId,
                p.name as name,
                t.code as tastingTypeCode,
                p.lat as lat,
                p.lng as lng,
                p.thumbnail_image_url as thumbnailImageUrl,
                p.address as address,
                p.description as description,
                p.note as note,
                ST_Distance_Sphere(point(p.lng, p.lat), point(:lng, :lat)) as distanceMeters
            from place p
            left join tasting_note_type t on t.id = p.tasting_type_id
            where (:q is null or p.name like concat('%', :q, '%'))
        ) x
        where (:lastDistance is null
            or x.distanceMeters > :lastDistance
            or (x.distanceMeters = :lastDistance and x.placeId > :lastId))
        order by x.distanceMeters asc, x.placeId asc
        limit :limit
        """, nativeQuery = true)
    List<PlaceDistanceView> findByDistanceWithCursor(@Param("lat") double lat,
                                                     @Param("lng") double lng,
                                                     @Param("q") String q,
                                                     @Param("lastDistance") Double lastDistance,
                                                     @Param("lastId") Long lastId,
                                                     @Param("limit") int limit);

    // H2 호환 거리 계산 (Haversine)
    @Query(value = """
        select * from (
            select
                p.place_id as placeId,
                p.name as name,
                t.code as tastingTypeCode,
                p.lat as lat,
                p.lng as lng,
                p.thumbnail_image_url as thumbnailImageUrl,
                p.address as address,
                p.description as description,
                p.note as note,
                6371000 * 2 * asin(sqrt(
                    power(sin(radians(:lat - p.lat) / 2), 2) +
                    cos(radians(:lat)) * cos(radians(p.lat)) *
                    power(sin(radians(:lng - p.lng) / 2), 2)
                )) as distanceMeters
            from place p
            left join tasting_note_type t on t.id = p.tasting_type_id
            where (:q is null or p.name like concat('%', :q, '%'))
        ) x
        where (:lastDistance is null
            or x.distanceMeters > :lastDistance
            or (x.distanceMeters = :lastDistance and x.placeId > :lastId))
        order by x.distanceMeters asc, x.placeId asc
        limit :limit
        """, nativeQuery = true)
    List<PlaceDistanceView> findByDistanceWithCursorH2(@Param("lat") double lat,
                                                       @Param("lng") double lng,
                                                       @Param("q") String q,
                                                       @Param("lastDistance") Double lastDistance,
                                                       @Param("lastId") Long lastId,
                                                       @Param("limit") int limit);

    @Query("""
        select p from Place p
        where p.lat between :southWestLat and :northEastLat
          and p.lng between :southWestLng and :northEastLng
        order by p.placeId asc
        """)
    List<Place> findByBounds(@Param("southWestLat") java.math.BigDecimal southWestLat,
                             @Param("southWestLng") java.math.BigDecimal southWestLng,
                             @Param("northEastLat") java.math.BigDecimal northEastLat,
                             @Param("northEastLng") java.math.BigDecimal northEastLng);
}
