package com.example.SeaTea.domain.place.repository;

import com.example.SeaTea.domain.place.entity.MemberSavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberSavedPlaceRepository extends JpaRepository<MemberSavedPlace, Long> {
    long countByPlace_PlaceId(Long placeId);

    boolean existsByMember_IdAndPlace_PlaceId(Long memberId, Long placeId);

    @Query("""
        select count(ms) from MemberSavedPlace ms
        where ms.member.id = :memberId
          and ms.place.tastingTypeId = :tastingTypeId
        """)
    long countByMemberIdAndTastingTypeId(@Param("memberId") Long memberId,
                                         @Param("tastingTypeId") Long tastingTypeId);
}
