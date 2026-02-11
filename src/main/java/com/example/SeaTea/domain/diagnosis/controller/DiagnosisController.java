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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

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
    @GetMapping("/me/history")
    public ApiResponse<Slice<DiagnosisHistoryResponseDTO>> getMyDiagnosisHistory(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "0") int page,
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
    /// ================== TEST ONLY ==================
    /// ⚠️ 운영/배포 반영 금지: memberId로 타인 데이터 접근 가능
    /// 필요 시 로컬에서만 잠깐 사용하고 바로 제거할 것
    /// ===============================================
//    // 임시 테스트용: memberId로 멤버 조회 후 진단 제출
//    @PostMapping("/test/detail")
//    public ApiResponse<DiagnosisDetailResponseDTO> submitDetailDiagnosisTest(
//            @RequestParam Long memberId,
//            @RequestBody @Valid DiagnosisDetailRequestDTO req
//    ) {
//        System.out.println(">>> diagnosis detail test called"); //호출 확인용
//        Member member = findMemberOrThrow(memberId);
//
//        return ApiResponse.onSuccess(diagnosisDetailService.submitDetailDiagnosis(member, req));
//        //성공이면 200 OK, DTO를 JSON으로 반환
//    }
//
//    // 임시 테스트용: memberId로 멤버 조회 후 간단 진단 제출
//    @PostMapping("/test/quick")
//    public ApiResponse<DiagnosisQuickResponseDTO> submitQuickDiagnosisTest(
//            @RequestParam Long memberId,
//            @RequestBody @Valid DiagnosisQuickRequestDTO req
//    ) {
//        System.out.println(">>> diagnosis quick test called"); // 호출 확인용
//
//        Member member = findMemberOrThrow(memberId);
//
//        return ApiResponse.onSuccess(diagnosisQuickService.submitQuickDiagnosis(member, req));
//    }
//
//    // 임시 테스트용: memberId로 멤버 조회 후 최신 이력
//    @GetMapping("/test/me")
//    public ApiResponse<DiagnosisResultResponseDTO> getMyDiagnosisResultTest(
//            @RequestParam Long memberId
//    ) {
//        Member member = findMemberOrThrow(memberId);
//        return ApiResponse.onSuccess(diagnosisResultService.getMyLatestResult(member));
//    }
//
//    //임시 테스트용: memberId로 멤버 조회 후 과거 진단내역 조회 (슬라이스로 페이징)
//    @GetMapping("/test/me/history")
//    public ApiResponse<Slice<DiagnosisHistoryResponseDTO>> getMyDiagnosisHistoryTest(
//            @RequestParam Long memberId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "3") int size
//    ) {
//        Member member = findMemberOrThrow(memberId);
//
//        Pageable pageable = PageRequest.of(
//                page,
//                size,
//                Sort.by(Sort.Direction.DESC, "createdAt")
//        );
//
//        return ApiResponse.onSuccess(diagnosisResultService.getMyHistory(member, pageable));
//    }

}