package com.example.SeaTea.domain.diagnosis.service;

import com.example.SeaTea.domain.diagnosis.converter.DiagnosisResponseConverter;
import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisSubmitResponseDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.diagnosis.enums.Mode;
import com.example.SeaTea.domain.diagnosis.enums.Status;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisResponseRepository;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisSessionRepository;
import com.example.SeaTea.domain.diagnosis.repository.TastingNoteTypeRepository;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisResultDecider;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisStep1;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisStep2;
import com.example.SeaTea.domain.member.entity.Member;
import com.example.SeaTea.global.exception.GeneralException;
import com.example.SeaTea.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiagnosisService {

    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final DiagnosisResponseRepository diagnosisResponseRepository;
    private final TastingNoteTypeRepository tastingNoteTypeRepository;

    /**
     * [상세 진단 제출]
     *
     * 흐름
     * 1) Step1 제출: 세션 생성 → Q1~Q4 저장 → Step1 점수 계산/판정
     *    - NEED_MORE: nextStep=2로 응답 (클라이언트는 같은 sessionId로 Step2 제출)
     *    - DONE: 결과 타입 확정 후 세션에 저장
     *
     * 2) Step2 제출: 세션 소유자 검증 후 조회 → Q5~Q8 저장 → Step1/Step2 점수로 최종 타입 결정 → 세션에 저장
     */
    public DiagnosisSubmitResponseDTO submitDetailDiagnosis(Member member, DiagnosisSubmitRequestDTO req) {

        if (req.getStep() == null) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        // =========================
        // STEP 1
        // =========================
        if (req.getStep() == 1) {

            // 1) 세션 생성 (상세 진단 세션)
            DiagnosisSession session = diagnosisSessionRepository.save(
                    DiagnosisSession.builder()
                            .member(member)
                            .mode(Mode.DETAIL)
                            .type(null)
                            .build()
            );

            // 2) Step1 응답 저장 (DTO → Entity 변환은 Converter 담당)
            List<DiagnosisResponse> step1Responses = DiagnosisResponseConverter.fromStep1(session, req);
            diagnosisResponseRepository.saveAll(step1Responses);

            // 3) Step1 점수 계산 (q1~q4 기반)
            Map<TastingNoteTypeCode, Integer> step1Scores = DiagnosisStep1.scoreStep1(
                    req.getQ1(),
                    req.getQ2(),
                    req.getQ3(),
                    req.getQ4()
            );

            // 4) Step1 판정 (DecideResult record 반환: status/nextStep/type)
            DiagnosisResultDecider.DecideResult result =
                    DiagnosisResultDecider.decideAfterStep1(step1Scores);

            log.warn("[STEP1 SCORES] {}", step1Scores);
            log.warn("[STEP1 DECIDE] {}", result);

            // 5) NEED_MORE면 Step2로 유도
            if (result.status() == Status.NEED_MORE) {
                return new DiagnosisSubmitResponseDTO(
                        "NEED_MORE",
                        result.nextStep(),  // 보통 2
                        null,
                        session.getId()
                );
            }

            // 6) DONE이면 결과 타입 확정 및 세션에 저장
            TastingNoteTypeCode decidedCode = result.type();
            String typeCodeStr = decidedCode.name(); // getCode() 없으니 name() 사용

            // 6-1) 결과 코드로 결과 타입 엔티티 조회
            TastingNoteType typeEntity = tastingNoteTypeRepository.findByCode(typeCodeStr)
                    // NOTE: 현재 ErrorStatus에 NOT_FOUND가 없어서 BAD_REQUEST로 처리 (원하면 NOT_FOUND 추가 권장)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

            diagnosisSessionRepository.save(
                    DiagnosisSession.builder()
                            .id(session.getId())
                            .member(session.getMember())
                            .mode(session.getMode())
                            .type(typeEntity)
                            .build()
            );

            return new DiagnosisSubmitResponseDTO(
                    "DONE",
                    null,
                    typeCodeStr,
                    session.getId()
            );
        }

        // =========================
        // STEP 2
        // =========================
        if (req.getSessionId() == null) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        // 1) 세션 조회 및 소유자 검증
        DiagnosisSession session = diagnosisSessionRepository
                .findByIdAndMemberId(req.getSessionId(), member.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        // 2) Step2 응답 저장 (DTO → Entity 변환은 Converter 담당)
        List<DiagnosisResponse> step2Responses = DiagnosisResponseConverter.fromStep2(session, req);
        diagnosisResponseRepository.saveAll(step2Responses);

        // 3) Step1/Step2 점수 계산
        // NOTE: Step2 요청에 q1~q4가 포함되지 않는 구조라면, Step1 응답을 DB에서 조회해 복원해야 함.
        //       (DiagnosisResponseRepository.findAllBySessionId(sessionId) 활용)
        Map<TastingNoteTypeCode, Integer> step1Scores = DiagnosisStep1.scoreStep1(
                req.getQ1(),
                req.getQ2(),
                req.getQ3(),
                req.getQ4()
        );

        // 3-2) Step2 점수 계산 (DTO 기반)
        Map<TastingNoteTypeCode, Integer> step2Scores = DiagnosisStep2.scoreStep2(req);

        // 4) Step1+Step2 점수로 최종 타입 결정
        TastingNoteTypeCode finalCode =
                DiagnosisResultDecider.decideFinal(req, step1Scores, step2Scores);

        String finalCodeStr = finalCode.name();

        // 5) 결과 타입 엔티티 조회 후 세션에 저장
        TastingNoteType finalType = tastingNoteTypeRepository.findByCode(finalCodeStr)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

        diagnosisSessionRepository.save(
                DiagnosisSession.builder()
                        .id(session.getId())
                        .member(session.getMember())
                        .mode(session.getMode())
                        .type(finalType)
                        .build()
        );

        return new DiagnosisSubmitResponseDTO(
                "DONE",
                null,
                finalCodeStr,
                session.getId()
        );
    }
}