package com.example.SeaTea.domain.diagnosis.controller;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisSubmitResponseDTO;
import com.example.SeaTea.domain.diagnosis.service.DiagnosisService;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.domain.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final MemberRepository memberRepository;

    // 임시 테스트용: memberId로 멤버 조회 후 진단 제출
    @PostMapping("/detail/test")
    public ResponseEntity<DiagnosisSubmitResponseDTO> submitDetailDiagnosisTest(
            @RequestParam Long memberId,
            @RequestBody @Valid DiagnosisSubmitRequestDTO req
    ) {
        System.out.println(">>> diagnosis detail test called"); //호출 확인용
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + memberId));
        //없는 멤버면 에러

        return ResponseEntity.ok(diagnosisService.submitDetailDiagnosis(member, req));
        //성공이면 200 OK, DTO를 JSON으로 반환
    }

//    // 나중에 인증 붙이면 이거로 교체
//    @PostMapping("/detail")
//    public ResponseEntity<DiagnosisSubmitResponseDTO> submitDetailDiagnosis(
//            @AuthenticationPrincipal Member member,
//            @RequestBody @Valid DiagnosisSubmitRequestDTO req
//    ) {
//        return ResponseEntity.ok(diagnosisService.submitDetailDiagnosis(member, req));
//    }
}