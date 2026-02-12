package com.example.SeaTea.domain.diagnosis.controller;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisDetailRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisDetailResponseDTO;
import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisQuickRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisHistoryResponseDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisQuickResponseDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisResultResponseDTO;
import com.example.SeaTea.domain.diagnosis.service.DiagnosisQuickService;
import com.example.SeaTea.domain.diagnosis.service.DiagnosisDetailService;
import com.example.SeaTea.domain.diagnosis.service.DiagnosisResultService;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import com.example.SeaTea.global.apiPayLoad.ApiResponse;
import com.example.SeaTea.global.auth.service.CustomUserDetails;
import com.example.SeaTea.global.exception.GeneralException;
import com.example.SeaTea.global.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

@Tag(name = "Diagnosis", description = "진단(상세/간단) 및 결과 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisDetailService diagnosisDetailService;
    private final MemberRepository memberRepository;
    private final DiagnosisQuickService diagnosisQuickService;
    private final DiagnosisResultService diagnosisResultService;

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));
    }

    //상세 진단
    @Operation(summary = "상세 진단 제출", description = "Step1 또는 Step2 상세 진단을 제출합니다.")
    @PostMapping("/detail")
    public ApiResponse<DiagnosisDetailResponseDTO> submitDetailDiagnosis(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid DiagnosisDetailRequestDTO req
    ) {
        if (customUserDetails == null) { //로그인안되면 COMMON401
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        Member member = customUserDetails.getMember();
        return ApiResponse.onSuccess(diagnosisDetailService.submitDetailDiagnosis(member, req));
    }

    //간단 진단
    @Operation(summary = "간단 진단 제출", description = "3개의 키워드를 기반으로 간단 진단을 수행합니다.")
    @PostMapping("/quick")
    public ApiResponse<DiagnosisQuickResponseDTO> submitQuickDiagnosis(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid DiagnosisQuickRequestDTO req
    ) {
        if (customUserDetails == null) { //로그인안되면 COMMON401
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        Member member = customUserDetails.getMember();
        return ApiResponse.onSuccess(diagnosisQuickService.submitQuickDiagnosis(member, req));
    }


    //최신 진단결과 조회
    @Operation(summary = "나의 최신 진단 결과 조회", description = "완료된 진단 중 가장 최근 결과를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<DiagnosisResultResponseDTO> getMyDiagnosisResult(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) { //로그인안되면 COMMON401
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        Member member = customUserDetails.getMember();
        return ApiResponse.onSuccess(diagnosisResultService.getMyLatestResult(member));
    }

    //과거 진단이력 조회 ( 슬라이스로 페이징 : 처음은 3개, 그 프론트에서 10개를 명시하면 됨.)
    @Operation(summary = "나의 진단 이력 조회", description = "완료된 진단 이력을 페이지 단위로 조회합니다.")
    @GetMapping("/me/history")
    public ApiResponse<Slice<DiagnosisHistoryResponseDTO>> getMyDiagnosisHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "3") int size
    ) {
        if (customUserDetails == null) { //로그인안되면 COMMON401
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        Member member = customUserDetails.getMember();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return ApiResponse.onSuccess(diagnosisResultService.getMyHistory(member, pageable));
    }
}