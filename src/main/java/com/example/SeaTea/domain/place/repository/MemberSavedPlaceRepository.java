package com.example.SeaTea.domain.place.repository;

import com.example.SeaTea.domain.place.entity.MemberSavedPlace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberSavedPlaceRepository extends JpaRepository<MemberSavedPlace, Long> {
    long countByPlace_PlaceId(Long placeId);

    boolean existsByMember_IdAndPlace_PlaceId(Long memberId, Long placeId);

    Optional<MemberSavedPlace> findByMember_IdAndPlace_PlaceId(Long memberId, Long placeId);

    @Query("""
        select msp from MemberSavedPlace msp
        join fetch msp.place p
        left join fetch p.tastingType
        where msp.member.id = :memberId
          and (:lastId is null or msp.memberSavedPlaceId > :lastId)
        order by msp.memberSavedPlaceId asc
        """)
    List<MemberSavedPlace> findByMemberIdWithCursor(@Param("memberId") Long memberId,
                                                    @Param("lastId") Long lastId,
                                                    Pageable pageable);

    @Query("""
        select count(ms) from MemberSavedPlace ms
        where ms.member.id = :memberId
          and ms.place.tastingTypeId = :tastingTypeId
        """)
    long countByMemberIdAndTastingTypeId(@Param("memberId") Long memberId,
                                         @Param("tastingTypeId") Long tastingTypeId);
}
