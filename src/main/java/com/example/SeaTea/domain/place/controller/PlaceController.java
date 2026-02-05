package com.example.SeaTea.domain.place.controller;

import com.example.SeaTea.domain.place.dto.SpaceListResponse;
import com.example.SeaTea.domain.place.service.PlaceQueryService;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spaces")
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
}
