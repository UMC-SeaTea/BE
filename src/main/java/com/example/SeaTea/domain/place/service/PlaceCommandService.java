package com.example.SeaTea.domain.place.service;

import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.place.dto.PlaceSaveResponse;
import com.example.SeaTea.domain.place.entity.MemberSavedPlace;
import com.example.SeaTea.domain.place.entity.Place;
import com.example.SeaTea.domain.place.repository.MemberSavedPlaceRepository;
import com.example.SeaTea.domain.place.repository.PlaceRepository;
import com.example.SeaTea.domain.place.status.SpaceErrorStatus;
import com.example.SeaTea.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceCommandService {

    private final PlaceRepository placeRepository;
    private final MemberSavedPlaceRepository memberSavedPlaceRepository;

    @Transactional
    public PlaceSaveResponse savePlace(Member member, Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(SpaceErrorStatus._NOT_FOUND));

        if (memberSavedPlaceRepository.existsByMember_IdAndPlace_PlaceId(member.getId(), placeId)) {
            return PlaceSaveResponse.saved();
        }

        MemberSavedPlace savedPlace = MemberSavedPlace.of(member, place);
        memberSavedPlaceRepository.save(savedPlace);
        return PlaceSaveResponse.saved();
    }

    @Transactional
    public PlaceSaveResponse unsavePlace(Member member, Long placeId) {
        placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(SpaceErrorStatus._NOT_FOUND));

        memberSavedPlaceRepository.findByMember_IdAndPlace_PlaceId(member.getId(), placeId)
                .ifPresent(memberSavedPlaceRepository::delete);

        return PlaceSaveResponse.unsaved();
    }
}
