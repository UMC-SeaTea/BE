package com.example.SeaTea.domain.place.repository;

import com.example.SeaTea.domain.place.entity.MemberRecentPlace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRecentPlaceRepository extends JpaRepository<MemberRecentPlace, Long> {

    Optional<MemberRecentPlace> findByMember_IdAndPlace_PlaceId(Long memberId, Long placeId);

    @Query("""
        select mrp from MemberRecentPlace mrp
        join fetch mrp.place p
        left join fetch p.tastingType
        where mrp.member.id = :memberId
        order by mrp.viewedAt desc, mrp.memberRecentPlaceId desc
        """)
    List<MemberRecentPlace> findByMemberIdFirstPage(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
        select mrp from MemberRecentPlace mrp
        join fetch mrp.place p
        left join fetch p.tastingType
        where mrp.member.id = :memberId
          and (mrp.viewedAt < :lastViewedAt
            or (mrp.viewedAt = :lastViewedAt and mrp.memberRecentPlaceId < :lastId))
        order by mrp.viewedAt desc, mrp.memberRecentPlaceId desc
        """)
    List<MemberRecentPlace> findByMemberIdWithCursor(
        @Param("memberId") Long memberId,
        @Param("lastViewedAt") java.time.LocalDateTime lastViewedAt,
        @Param("lastId") Long lastId,
        Pageable pageable
    );
}
