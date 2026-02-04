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
import com.example.SeaTea.global.exception.GeneralException;
import com.example.SeaTea.global.status.ErrorStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // 임시 테스트용: memberId로 멤버 조회 후 진단 제출
    @PostMapping("/detail/test")
    public ApiResponse<DiagnosisDetailResponseDTO> submitDetailDiagnosisTest(
            @RequestParam Long memberId,
            @RequestBody @Valid DiagnosisDetailRequestDTO req
    ) {
        System.out.println(">>> diagnosis detail test called"); //호출 확인용
        Member member = findMemberOrThrow(memberId);

        return ApiResponse.onSuccess(diagnosisDetailService.submitDetailDiagnosis(member, req));
        //성공이면 200 OK, DTO를 JSON으로 반환
    }

    // 임시 테스트용: memberId로 멤버 조회 후 간단 진단 제출
    @PostMapping("/quick/test")
    public ApiResponse<DiagnosisQuickResponseDTO> submitQuickDiagnosisTest(
            @RequestParam Long memberId,
            @RequestBody @Valid DiagnosisQuickRequestDTO req
    ) {
        System.out.println(">>> diagnosis quick test called"); // 호출 확인용

        Member member = findMemberOrThrow(memberId);

        return ApiResponse.onSuccess(diagnosisQuickService.submitQuickDiagnosis(member, req));
    }

//    // 나중에 인증 붙이면 이거로 교체
//    @PostMapping("/detail")
//    public ApiResponse<DiagnosisDetailResponseDTO> submitDetailDiagnosis(
//            @AuthenticationPrincipal Member member,
//            @RequestBody @Valid DiagnosisDetailRequestDTO req
//    ) {
//        return ApiResponse.onSuccess(diagnosisDetailService.submitDetailDiagnosis(member, req));
//    }

//    // 나중에 인증 붙이면 이거로 교체
//    @PostMapping("/quick")
//    public ApiResponse<DiagnosisQuickResponseDTO> submitQuickDiagnosis(
//            @AuthenticationPrincipal Member member,
//            @RequestBody @Valid DiagnosisQuickRequestDTO req
//    ) {
//        return ApiResponse.onSuccess(diagnosisQuickService.submitQuickDiagnosis(member, req));
//    }

    //내 최신 진단 조회
    @GetMapping("/me/test")
    public ApiResponse<DiagnosisResultResponseDTO> getMyDiagnosisResultTest(
            @RequestParam Long memberId
    ) {
        Member member = findMemberOrThrow(memberId);
        return ApiResponse.onSuccess(diagnosisResultService.getMyLatestResult(member));
    }

    //과거 진단내역 조회 (슬라이스로 페이징)
    @GetMapping("/me/history/test")
    public ApiResponse<Slice<DiagnosisHistoryResponseDTO>> getMyDiagnosisHistoryTest(
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Member member = findMemberOrThrow(memberId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ApiResponse.onSuccess(diagnosisResultService.getMyHistory(member, pageable));
    }

    //나중에 인증붙으면 교체
    // @GetMapping("/me")
    // public ApiResponse<DiagnosisResultResponseDTO> getMyDiagnosisResult(
    //         @AuthenticationPrincipal Member member
    // ) {
    //     return ApiResponse.onSuccess(diagnosisResultService.getMyLatestResult(member));
    // }
    //
    // @GetMapping("/me/history")
    // public ApiResponse<Slice<DiagnosisHistoryResponseDTO>> getMyDiagnosisHistory(
    //         @AuthenticationPrincipal Member member,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size
    // ) {
    //     Pageable pageable = PageRequest.of(
    //             page,
    //             size,
    //             Sort.by(Sort.Direction.DESC, "createdAt")
    //     );
    //     return ApiResponse.onSuccess(diagnosisResultService.getMyHistory(member, pageable));
    // }
}