package com.example.SeaTea.domain.place.controller;

import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import com.example.SeaTea.domain.place.dto.SpaceListResponse;
import com.example.SeaTea.domain.place.service.PlaceQueryService;
import com.example.SeaTea.domain.place.dto.SpaceDetailResponse;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.global.auth.CustomUserDetails;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@Tag(name = "공간 조회 API")
public class PlaceController {

    private final PlaceQueryService placeQueryService;

    @GetMapping
    @Operation(summary = "공간 목록 조회", description = "검색어/위치/커서 기반으로 공간 목록을 조회합니다.")
    public ApiResponse<SpaceListResponse> getSpaces(
        @Parameter(description = "위도 (위치 기반 조회 시 필수)")
        @RequestParam(required = false) Double lat,
        @Parameter(description = "경도 (위치 기반 조회 시 필수)")
        @RequestParam(required = false) Double lng,
        @Parameter(description = "검색어 (공간명 부분 일치)")
        @RequestParam(required = false) String q,
        @Parameter(description = "페이지 크기 (기본 20, 최대 100)")
        @RequestParam(required = false) Integer size,
        @Parameter(description = "커서 토큰 (응답의 nextCursor 그대로 전달)")
        @RequestParam(required = false) String cursor
    ) {
        return ApiResponse.onSuccess(
            placeQueryService.getSpaces(lat, lng, q, size, cursor)
        );
    }

    //공간 추천
    @GetMapping("/recommend")
    @Operation(summary = "공간 추천", description = "휴식유형(tastingTypeCode)에 해당하는 공간을 커서 기반으로 추천합니다.")
    public ApiResponse<SpaceListResponse> getRecommendedSpaces(
        @Parameter(description = "휴식유형 코드 (예: SMOKY)")
        @RequestParam TastingNoteTypeCode tastingTypeCode,
        @Parameter(description = "위도 (거리 표시 시 필요)")
        @RequestParam(required = false) Double lat,
        @Parameter(description = "경도 (거리 표시 시 필요)")
        @RequestParam(required = false) Double lng,
        @Parameter(description = "페이지 크기 (기본 20, 최대 100)")
        @RequestParam(required = false) Integer size,
        @Parameter(description = "커서 토큰 (응답의 nextCursor 그대로 전달)")
        @RequestParam(required = false) String cursor
    ) {
        return ApiResponse.onSuccess(
            placeQueryService.getRecommendedSpaces(tastingTypeCode.name(), lat, lng, size, cursor)
        );
    }

    @GetMapping("/{spaceId}")
    @Operation(summary = "공간 상세 조회", description = "공간 상세 정보를 조회합니다.")
    public ApiResponse<SpaceDetailResponse> getSpaceDetail(
        @Parameter(description = "공간 ID")
        @PathVariable Long spaceId,
        @Parameter(description = "위도 (거리 계산 시 필요)")
        @RequestParam(required = false) Double lat,
        @Parameter(description = "경도 (거리 계산 시 필요)")
        @RequestParam(required = false) Double lng,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = userDetails == null ? null : userDetails.getMember();
        return ApiResponse.onSuccess(
            placeQueryService.getSpaceDetail(spaceId, lat, lng, member)
        );
    }
}
