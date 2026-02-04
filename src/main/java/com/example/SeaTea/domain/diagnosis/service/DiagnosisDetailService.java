package com.example.SeaTea.domain.diagnosis.service;

import com.example.SeaTea.domain.diagnosis.converter.DiagnosisDetailConverter;
import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisDetailRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisDetailResponseDTO;
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
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisException;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiagnosisDetailService {

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
    public DiagnosisDetailResponseDTO submitDetailDiagnosis(Member member, DiagnosisDetailRequestDTO req) {

        if (req.getStep() == null || (req.getStep() != 1 && req.getStep() != 2)) {
            throw new DiagnosisException(DiagnosisErrorStatus._INVALID_STEP); //step이 null,1,2가 아니면 예외
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
            List<DiagnosisResponse> step1Responses = DiagnosisDetailConverter.fromStep1(session, req);
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
                return new DiagnosisDetailResponseDTO(
                        "NEED_MORE",
                        result.nextStep(),  // 2
                        null,
                        session.getId()
                );
            }

            // 6) DONE이면 결과 타입 확정 및 세션에 저장
            TastingNoteTypeCode decidedCode = result.type();
            String typeCodeStr = decidedCode.name(); // getCode() 없으니 name() 사용

            // 6-1) 결과 코드로 결과 타입 엔티티 조회
            TastingNoteType typeEntity = tastingNoteTypeRepository.findByCode(typeCodeStr)
                    .orElseThrow(() -> new DiagnosisException(DiagnosisErrorStatus._TYPE_NOT_FOUND));// 타입 조회 실패

            // 이미 영속 상태인 session 엔티티를 업데이트(더티체킹)
            session.updateType(typeEntity);

            return new DiagnosisDetailResponseDTO(
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
            throw new DiagnosisException(DiagnosisErrorStatus._INVALID_STEP);//nul,1,2가 아니면 예외
        }

        // 1) 세션 조회 및 소유자 검증
        DiagnosisSession session = diagnosisSessionRepository
                .findByIdAndMemberId(req.getSessionId(), member.getId())
                .orElseThrow(() -> new DiagnosisException(DiagnosisErrorStatus._SESSION_NOT_FOUND));//세션 id 없음.

        // 2) Step2 응답 저장 (DTO → Entity 변환은 Converter 담당)
        List<DiagnosisResponse> step2Responses = DiagnosisDetailConverter.fromStep2(session, req);
        diagnosisResponseRepository.saveAll(step2Responses);

        // 3) Step1/Step2 점수 계산
        // Step2 요청에는 q1~q4가 포함되지 않으므로, DB에 저장된 Step1(Q1~Q4) 응답을 복원해서 Step1 점수를 계산한다.
        List<DiagnosisResponse> savedStep1Responses = diagnosisResponseRepository.findAllBySessionId(session.getId());
        DiagnosisDetailConverter.Step1Answers step1 = DiagnosisDetailConverter.restoreStep1Answers(savedStep1Responses);

        Map<TastingNoteTypeCode, Integer> step1Scores = DiagnosisStep1.scoreStep1(
                step1.q1(),
                step1.q2(),
                step1.q3(),
                step1.q4()
        );

        // 3-2) Step2 점수 계산 (DTO 기반)
        Map<TastingNoteTypeCode, Integer> step2Scores = DiagnosisStep2.scoreStep2(req);

        // Step2 점수 및 합산 점수 로그 (Step1 로그는 Step1 요청 시 이미 출력되므로 Step2에서는 추가 출력만)
        Map<TastingNoteTypeCode, Integer> totalScores = mergeScores(step1Scores, step2Scores);

        log.warn("[STEP2 SCORES] {}", step2Scores);
        log.warn("[TOTAL SCORES] {}", totalScores);

        // 4) Step1+Step2 점수로 최종 타입 결정
        TastingNoteTypeCode finalCode =
                DiagnosisResultDecider.decideFinal(req, step1Scores, step2Scores);

        String finalCodeStr = finalCode.name();

        // 5) 결과 타입 엔티티 조회 후 세션에 저장
        TastingNoteType finalType = tastingNoteTypeRepository.findByCode(finalCodeStr)
                .orElseThrow(() -> new DiagnosisException(DiagnosisErrorStatus._TYPE_NOT_FOUND));//타입 조회 실패

        // 이미 조회된 session 엔티티를 업데이트(더티체킹)
        session.updateType(finalType);

        return new DiagnosisDetailResponseDTO(
                "DONE",
                null,
                finalCodeStr,
                session.getId()
        );
    }
    /**
     * Step1/Step2 점수 맵을 합산해서 반환한다.
     *
     * - 로그 가독성을 위해 TastingNoteTypeCode enum 선언 순서대로 정렬된 Map(LinkedHashMap)으로 반환한다.
     */
    private Map<TastingNoteTypeCode, Integer> mergeScores(
            Map<TastingNoteTypeCode, Integer> step1Scores,
            Map<TastingNoteTypeCode, Integer> step2Scores
    ) {
        // enum 선언 순서대로 고정 출력되도록 LinkedHashMap 사용
        Map<TastingNoteTypeCode, Integer> merged = new LinkedHashMap<>();

        // 먼저 모든 타입을 0으로 채워서 순서를 고정
        for (TastingNoteTypeCode code : TastingNoteTypeCode.values()) {
            merged.put(code, 0);
        }

        // Step1 점수 반영
        if (step1Scores != null) {
            step1Scores.forEach((k, v) -> merged.merge(k, v, Integer::sum));
        }

        // Step2 점수 반영
        if (step2Scores != null) {
            step2Scores.forEach((k, v) -> merged.merge(k, v, Integer::sum));
        }

        return merged;
    }
}