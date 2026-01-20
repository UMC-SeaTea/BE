package com.example.SeaTea.domain.diagnosis.service;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.dto.response.DiagnosisSubmitResponseDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;
import com.example.SeaTea.domain.diagnosis.entity.TastingNoteType;
import com.example.SeaTea.domain.diagnosis.enums.Mode;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisResponseRepository;
import com.example.SeaTea.domain.diagnosis.repository.DiagnosisSessionRepository;
import com.example.SeaTea.domain.diagnosis.repository.TastingNoteTypeRepository;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisResultDecider;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisScoringUtil;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisStep1;
import com.example.SeaTea.domain.diagnosis.scoring.DiagnosisStep2;
import com.example.SeaTea.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisService {

    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final DiagnosisResponseRepository diagnosisResponseRepository;
    private final TastingNoteTypeRepository tastingNoteTypeRepository;

    /**
     * Step1 저장값 복원용 (DTO setter 없이도 깔끔하게 처리)
     */
    private record Step1Answers(String q1, String q2, Integer q3, List<String> q4) {}

    public DiagnosisSubmitResponseDTO submitDetailDiagnosis(Member member, DiagnosisSubmitRequestDTO req) {
        if (req.getStep() == null) {
            throw new IllegalArgumentException("step은 필수입니다.");
        }

        // ===================== STEP 1 =====================
        if (req.getStep() == 1) {
            validateStep1(req);

            // 1) 세션 생성 (type은 nullable로 운영)
            DiagnosisSession session = DiagnosisSession.builder()
                    .member(member)
                    .mode(Mode.DETAIL)
                    .type(null)
                    .build();
            DiagnosisSession savedSession = diagnosisSessionRepository.save(session);

            // 2) 응답 저장(Q1~Q4)
            saveResponse(savedSession, "Q1", req.getQ1());
            saveResponse(savedSession, "Q2", req.getQ2());
            saveResponse(savedSession, "Q3", String.valueOf(req.getQ3()));
            saveResponse(savedSession, "Q4", String.join(",", req.getQ4())); // "ALONE,COUPLE"

            // 3) step1 점수 계산 + 판정
            Map<TastingNoteTypeCode, Integer> step1Scores =
                    DiagnosisStep1.scoreStep1(req.getQ1(), req.getQ2(), req.getQ3(), req.getQ4());

            DiagnosisResultDecider.DecideResult decide = DiagnosisResultDecider.decideAfterStep1(step1Scores);

            // 4) DONE이면 session.type 확정 후 응답
            if (decide.status() == DiagnosisResultDecider.Status.DONE) {
                TastingNoteType type = tastingNoteTypeRepository.findByCode(decide.type().name())
                        .orElseThrow(() -> new IllegalArgumentException("TastingNoteType not found: " + decide.type().name()));

                // update (builder로 새 객체 만들지 말고, 더티체킹하려면 setter가 필요하지만 현재 구조상 save로 갱신)
                DiagnosisSession updated = DiagnosisSession.builder()
                        .id(savedSession.getId())
                        .member(savedSession.getMember())
                        .mode(savedSession.getMode())
                        .type(type)
                        .build();
                diagnosisSessionRepository.save(updated);

                return new DiagnosisSubmitResponseDTO("DONE", null, type.getCode(), savedSession.getId());
            }

            // NEED_MORE면 sessionId 반환
            return new DiagnosisSubmitResponseDTO("NEED_MORE", 2, null, savedSession.getId());
        }

        // ===================== STEP 2 =====================
        if (req.getStep() == 2) {
            validateStep2(req);

            if (req.getSessionId() == null) {
                throw new IllegalArgumentException("step2에서는 sessionId가 필수입니다.");
            }

            // ✅ 본인 세션만 접근 가능
            DiagnosisSession session = diagnosisSessionRepository
                    .findByIdAndMemberId(req.getSessionId(), member.getId())
                    .orElseThrow(() -> new IllegalArgumentException("본인 세션만 조회/제출 가능합니다. sessionId=" + req.getSessionId()));

            // 1) step1 답변 복원 (DB에서 Q1~Q4 가져오기)
            Step1Answers step1 = reconstructStep1Answers(session.getId());

            // 2) 점수 계산
            Map<TastingNoteTypeCode, Integer> step1Scores =
                    DiagnosisStep1.scoreStep1(step1.q1(), step1.q2(), step1.q3(), step1.q4());
            Map<TastingNoteTypeCode, Integer> step2Scores = DiagnosisStep2.scoreStep2(req);

            // 3) 최종 타입 결정 (동점 규칙 포함)
            Integer energyForTieBreak = (req.getQ3() != null) ? req.getQ3() : step1.q3();
            TastingNoteTypeCode finalTypeCode = decideFinalByEnergy(energyForTieBreak, step1Scores, step2Scores);

            // 4) step2 응답 저장(Q5~Q8)
            saveResponse(session, "Q5", req.getQ5());
            saveResponse(session, "Q6", req.getQ6());
            saveResponse(session, "Q7", req.getQ7());
            saveResponse(session, "Q8", req.getQ8());

            // 5) session.type 업데이트
            TastingNoteType type = tastingNoteTypeRepository.findByCode(finalTypeCode.name())
                    .orElseThrow(() -> new IllegalArgumentException("TastingNoteType not found: " + finalTypeCode.name()));

            DiagnosisSession updated = DiagnosisSession.builder()
                    .id(session.getId())
                    .member(session.getMember())
                    .mode(session.getMode())
                    .type(type)
                    .build();
            diagnosisSessionRepository.save(updated);

            return new DiagnosisSubmitResponseDTO("DONE", null, type.getCode(), session.getId());
        }

        throw new IllegalArgumentException("step 값은 1 또는 2만 가능합니다.");
    }

    private void saveResponse(DiagnosisSession session, String itemCode, String answerCode) {
        DiagnosisResponse r = DiagnosisResponse.builder()
                .session(session)
                .itemCode(itemCode)
                .answerCode(answerCode)
                .build();
        diagnosisResponseRepository.save(r);
    }

    /**
     * DB에 저장된 Step1(Q1~Q4) 응답을 복원한다.
     * - Q4는 "ALONE,COUPLE" 형태로 저장되어 있으므로 split 처리
     */
    private Step1Answers reconstructStep1Answers(Long sessionId) {
        // 네 레포에 맞춰 그대로 사용
        List<DiagnosisResponse> responses = diagnosisResponseRepository.findAllBySessionId(sessionId);

        String q1 = findAnswer(responses, "Q1");
        String q2 = findAnswer(responses, "Q2");
        Integer q3 = Integer.valueOf(findAnswer(responses, "Q3"));

        String q4Str = findAnswer(responses, "Q4"); // "ALONE,COUPLE"
        List<String> q4 = Arrays.stream(q4Str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return new Step1Answers(q1, q2, q3, q4);
    }

    private String findAnswer(List<DiagnosisResponse> responses, String itemCode) {
        return responses.stream()
                .filter(r -> itemCode.equals(r.getItemCode()))
                .map(DiagnosisResponse::getAnswerCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("저장된 응답이 없습니다: " + itemCode));
    }

    /**
     * 최종 타입 결정 규칙:
     * 1) step1+step2 합산 후 단독 1등이면 그 타입
     * 2) 동점이면 step2 점수가 더 높은 타입 우선
     * 3) 그래도 동점이면 energy(q3) 우선순위로 결정
     */
    private TastingNoteTypeCode decideFinalByEnergy(
            Integer energy,
            Map<TastingNoteTypeCode, Integer> step1Scores,
            Map<TastingNoteTypeCode, Integer> step2Scores
    ) {
        Map<TastingNoteTypeCode, Integer> total = DiagnosisScoringUtil.merge(step1Scores, step2Scores);
        List<TastingNoteTypeCode> top = DiagnosisScoringUtil.findTopTypes(total);
        if (top.size() == 1) return top.get(0);

        // 1) 동점이면 step2 점수 높은 타입 우선
        int bestS2 = Integer.MIN_VALUE;
        List<TastingNoteTypeCode> filtered = new ArrayList<>();
        for (TastingNoteTypeCode t : top) {
            int s2 = DiagnosisScoringUtil.getScore(step2Scores, t);
            if (s2 > bestS2) {
                bestS2 = s2;
                filtered.clear();
                filtered.add(t);
            } else if (s2 == bestS2) {
                filtered.add(t);
            }
        }
        if (filtered.size() == 1) return filtered.get(0);

        // 2) 그래도 동점이면 energy 기반 고정 우선순위
        return tieBreakByEnergy(energy, filtered);
    }

    private TastingNoteTypeCode tieBreakByEnergy(Integer q3Energy, List<TastingNoteTypeCode> candidates) {
        int e = (q3Energy == null) ? 50 : q3Energy;

        List<TastingNoteTypeCode> priority;
        if (e <= 33) {
            priority = List.of(
                    TastingNoteTypeCode.SMOKY,
                    TastingNoteTypeCode.NUTTY,
                    TastingNoteTypeCode.EARTHY,
                    TastingNoteTypeCode.OCEANIC,
                    TastingNoteTypeCode.SWEET,
                    TastingNoteTypeCode.FLORAL,
                    TastingNoteTypeCode.FRUITY,
                    TastingNoteTypeCode.SPICES
            );
        } else if (e <= 66) {
            priority = List.of(
                    TastingNoteTypeCode.OCEANIC,
                    TastingNoteTypeCode.NUTTY,
                    TastingNoteTypeCode.EARTHY,
                    TastingNoteTypeCode.SWEET,
                    TastingNoteTypeCode.SMOKY,
                    TastingNoteTypeCode.FLORAL,
                    TastingNoteTypeCode.FRUITY,
                    TastingNoteTypeCode.SPICES
            );
        } else {
            priority = List.of(
                    TastingNoteTypeCode.FRUITY,
                    TastingNoteTypeCode.SPICES,
                    TastingNoteTypeCode.FLORAL,
                    TastingNoteTypeCode.SWEET,
                    TastingNoteTypeCode.OCEANIC,
                    TastingNoteTypeCode.NUTTY,
                    TastingNoteTypeCode.EARTHY,
                    TastingNoteTypeCode.SMOKY
            );
        }

        for (TastingNoteTypeCode p : priority) {
            if (candidates.contains(p)) return p;
        }
        return candidates.get(0); // fallback
    }

    private void validateStep1(DiagnosisSubmitRequestDTO req) {
        if (isBlank(req.getQ1()) || isBlank(req.getQ2())) {
            throw new IllegalArgumentException("step1에서는 q1, q2가 필수입니다.");
        }
        if (req.getQ3() == null) {
            throw new IllegalArgumentException("step1에서는 q3가 필수입니다.");
        }
        if (req.getQ4() == null || req.getQ4().isEmpty()) {
            throw new IllegalArgumentException("step1에서는 q4가 최소 1개 필요합니다.");
        }
    }

    private void validateStep2(DiagnosisSubmitRequestDTO req) {
        if (isBlank(req.getQ5()) || isBlank(req.getQ6()) || isBlank(req.getQ7()) || isBlank(req.getQ8())) {
            throw new IllegalArgumentException("step2에서는 q5~q8이 필수입니다.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}