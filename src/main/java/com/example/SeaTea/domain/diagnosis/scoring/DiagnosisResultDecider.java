package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.*;

public class DiagnosisResultDecider {

    public enum Status { DONE, NEED_MORE }

    public record DecideResult(Status status, Integer nextStep, TastingNoteTypeCode type) {}

    // Step1 판정: DONE vs NEED_MORE
    public static DecideResult decideAfterStep1(Map<TastingNoteTypeCode, Integer> step1Scores) {
        // 1위 타입들
        List<TastingNoteTypeCode> top = DiagnosisScoringUtil.findTopTypes(step1Scores);
        if (top.size() > 1) {
            return new DecideResult(Status.NEED_MORE, 2, null);
        }

        // 2위 점수 구하기
        int topScore = DiagnosisScoringUtil.getScore(step1Scores, top.get(0));
        int secondScore = Integer.MIN_VALUE;
        for (var e : step1Scores.entrySet()) {
            if (e.getKey() == top.get(0)) continue;
            secondScore = Math.max(secondScore, e.getValue());
        }

        // 점수차 >= 2 이면 DONE
        if (topScore - secondScore >= 2) {
            return new DecideResult(Status.DONE, null, top.get(0));
        }

        return new DecideResult(Status.NEED_MORE, 2, null);
    }

    // Step2까지 반영해서 최종 타입 1개 확정
    public static TastingNoteTypeCode decideFinal(
            DiagnosisSubmitRequestDTO req,
            Map<TastingNoteTypeCode, Integer> step1Scores,
            Map<TastingNoteTypeCode, Integer> step2Scores
    ) {
        var total = DiagnosisScoringUtil.merge(step1Scores, step2Scores);
        List<TastingNoteTypeCode> top = DiagnosisScoringUtil.findTopTypes(total);
        if (top.size() == 1) return top.get(0);

        // 1) 동점이면 Step2 점수 높은 타입 우선
        int bestStep2 = Integer.MIN_VALUE;
        List<TastingNoteTypeCode> filtered = new ArrayList<>();
        for (TastingNoteTypeCode t : top) {
            int s2 = DiagnosisScoringUtil.getScore(step2Scores, t);
            if (s2 > bestStep2) {
                bestStep2 = s2;
                filtered.clear();
                filtered.add(t);
            } else if (s2 == bestStep2) {
                filtered.add(t);
            }
        }
        if (filtered.size() == 1) return filtered.get(0);

        // 2) 그래도 동점이면 Q3 우선순위로 1개 선택
        return tieBreakByQ3(req.getQ3(), filtered);
    }

    private static TastingNoteTypeCode tieBreakByQ3(Integer q3Energy, List<TastingNoteTypeCode> candidates) {
        // q3가 없으면 그냥 고정 우선순위로 (안전하게 determinism 확보)
        int energy = (q3Energy == null) ? 50 : q3Energy;

        // 너가 팀이랑 합의해서 우선순위만 바꾸면 됨.
        List<TastingNoteTypeCode> priority;
        if (energy <= 33) {
            // 낮은 에너지: 조용/안정 우선
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
        } else if (energy <= 66) {
            // 중간: OCEANIC 중심 (원하면 수정)
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
            // 높은 에너지: 활동/자극 우선
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

        // 혹시 모를 fallback
        return candidates.get(0);
    }
}