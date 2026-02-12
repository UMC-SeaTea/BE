package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.Status;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 누적 점수를 기반으로 분기 결정 */
public class DiagnosisResultDecider {

    // 진단 점수 로직의 판단 결과를 담는 객체
    public record DecideResult(Status status, Integer nextStep, TastingNoteTypeCode type) {}

    /** Step1 판정: DONE 또는 NEED_MORE */
    public static DecideResult decideAfterStep1(Map<TastingNoteTypeCode, Integer> step1Scores) {
        // 1위 타입들
        List<TastingNoteTypeCode> top = DiagnosisScoringUtil.findTopTypes(step1Scores); // 1위 타입 찾기
        if (top.size() > 1) {
            return new DecideResult(Status.NEED_MORE, 2, null);
        } // 1위가 2개 이상이면 step2로 분기해야함

        // 1위가 1개면 2위와의 점수 차가 2 이상이어야 함.
        // 2위 점수 구하기
        int topScore = DiagnosisScoringUtil.getScore(step1Scores, top.get(0));
        int secondScore = Integer.MIN_VALUE; // 1위 타입을 제외하고 나머지 타입 중 가장 높은 점수가 2위
        for (var e : step1Scores.entrySet()) {
            if (e.getKey() == top.get(0)) continue;
            secondScore = Math.max(secondScore, e.getValue());
        }

        // 점수차 >= 2 이면 DONE
        if (topScore - secondScore >= 2) {
            return new DecideResult(Status.DONE, null, top.get(0));
        } // 점수차가 2 이상이면 끝!

        // 나머지는 모두 step2 필요
        return new DecideResult(Status.NEED_MORE, 2, null);
    }

    /** Step2까지 반영해서 최종 타입 1개 확정 */
    // step1, step2: 각 단계에서 나온 타입별 점수, q3Energy: 타이브레이커용 Q3 값
    public static TastingNoteTypeCode decideFinal(
            Integer q3Energy,
            Map<TastingNoteTypeCode, Integer> step1Scores,
            Map<TastingNoteTypeCode, Integer> step2Scores
    ) {
        var total = DiagnosisScoringUtil.merge(step1Scores, step2Scores); // 각 분기별 점수 합산

        List<TastingNoteTypeCode> top = DiagnosisScoringUtil.findTopTypes(total); // 1등 후보들 리스트
        if (top.size() == 1) return top.get(0); // 타입 한 개면 끝

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
        return tieBreakByQ3(q3Energy, filtered);
    }

    // Q3을 이용한 타이브레이커
    // q3Energy: 사용자의 응답, candidates: 점수 공동 1등 타입들
    private static TastingNoteTypeCode tieBreakByQ3(Integer q3Energy, List<TastingNoteTypeCode> candidates) {
        int energy = q3Energy;

        List<TastingNoteTypeCode> priority; // 우선순위를 가지는 타입 저장용

        if (energy <= 30) {
            // Q3 점수 반영: SMOKY(+2) > NUTTY(+1) = EARTHY(+1)
            priority = List.of(
                    TastingNoteTypeCode.SMOKY,
                    TastingNoteTypeCode.NUTTY,
                    TastingNoteTypeCode.EARTHY,
                    // 이하 fallback 우선순위(결과 결정 안정성용)
                    TastingNoteTypeCode.OCEANIC,
                    TastingNoteTypeCode.SWEET,
                    TastingNoteTypeCode.FLORAL,
                    TastingNoteTypeCode.FRUITY,
                    TastingNoteTypeCode.SPICES
            );
        } else if (energy <= 60) {
            // Q3 점수 반영: OCEANIC(+1) = SWEET(+1) = FLORAL(+1)
            // 동급이라 대표 우선순위는 팀 합의(여기선 OCEANIC 먼저)
            priority = List.of(
                    TastingNoteTypeCode.OCEANIC,
                    TastingNoteTypeCode.SWEET,
                    TastingNoteTypeCode.FLORAL,
                    // 이하 fallback
                    TastingNoteTypeCode.SMOKY,
                    TastingNoteTypeCode.NUTTY,
                    TastingNoteTypeCode.EARTHY,
                    TastingNoteTypeCode.FRUITY,
                    TastingNoteTypeCode.SPICES
            );
        } else {
            // Q3 점수 반영: FRUITY(+2) > SPICES(+1) = FLORAL(+1)
            priority = List.of(
                    TastingNoteTypeCode.FRUITY,
                    TastingNoteTypeCode.SPICES,
                    TastingNoteTypeCode.FLORAL,
                    // 이하 fallback
                    TastingNoteTypeCode.SWEET,
                    TastingNoteTypeCode.OCEANIC,
                    TastingNoteTypeCode.NUTTY,
                    TastingNoteTypeCode.EARTHY,
                    TastingNoteTypeCode.SMOKY
            );
        }

        // 후보군 중에서 가장 먼저 매칭되는 타입을 선택
        for (TastingNoteTypeCode p : priority) {
            if (candidates.contains(p)) return p;
        }
        return candidates.get(0); // fallback : 그래도 못 고를 경우 null 방지용
    }
}