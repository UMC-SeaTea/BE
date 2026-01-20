package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DiagnosisStep1 {

    /**
     * Step1(Q1~Q4) 점수 계산
     * - Q1: A/B (각 타입 +1)
     * - Q2: A/B (각 타입 +1)
     * - Q3: 0~100 에너지 (현재는 "tie-break 힌트" + 가벼운 가산점 용도로만 사용)
     * - Q4: 1~2개 선택 (각 선택지마다 해당 타입 +1)
     */
    public static Map<TastingNoteTypeCode, Integer> scoreStep1(
            String q1,
            String q2,
            Integer q3,
            List<String> q4
    ) {
        EnumMap<TastingNoteTypeCode, Integer> scores = initScores();

        applyQ1(scores, q1);
        applyQ2(scores, q2);

        if (q3 != null) {
            applyQ3Energy(scores, q3);
        }

        applyQ4(scores, q4);

        return scores;
    }

    private static EnumMap<TastingNoteTypeCode, Integer> initScores() {
        EnumMap<TastingNoteTypeCode, Integer> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode t : TastingNoteTypeCode.values()) {
            scores.put(t, 0);
        }
        return scores;
    }

    // Q1. 지금 휴식이 필요한 이유
    private static void applyQ1(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        } else if ("B".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.FLORAL, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        }
    }

    // Q2. 가장 최근 휴식 모습
    private static void applyQ2(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        } else if ("B".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        }
    }

    /**
     * Q3. 에너지 레벨 (0~100)
     * PM 문서에서 "tie-break 시 Q3 영향 타입 우선"이 있어서,
     * 여기서는 아주 가볍게만 가산점을 줘도 되고(현재 방식),
     * 아니면 아예 저장만 하고 tie-break에서만 쓰도록 변경해도 됨.
     */
    private static void applyQ3Energy(Map<TastingNoteTypeCode, Integer> scores, int energy) {
        // 낮은 에너지(0~33): 조용/안정 계열에 +1
        if (energy <= 33) {
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
        }
        // 중간(34~66): 중립/균형 느낌으로 Oceanic에 +1 (원하면 제거 가능)
        else if (energy <= 66) {
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        }
        // 높은 에너지(67~100): 활동/자극 계열에 +1
        else {
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.FLORAL, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        }
    }

    // Q4. 누구와 함께 휴식? (중복 가능, 최대 2개)
    private static void applyQ4(Map<TastingNoteTypeCode, Integer> scores, List<String> answers) {
        if (answers == null) return;

        for (String a : answers) {
            if (a == null) continue;

            // 프론트에서 값은 "ALONE", "COUPLE", "FAMILY", "ANYONE" 같은 코드로 보내는 걸 추천.
            // 일단 지금은 문자열로 들어온다고 가정하고 널널하게 처리.
            String key = a.trim();

            if (key.equalsIgnoreCase("ALONE")) {
                add(scores, TastingNoteTypeCode.SMOKY, 1);
                add(scores, TastingNoteTypeCode.NUTTY, 1);
                add(scores, TastingNoteTypeCode.OCEANIC, 1);
            } else if (key.equalsIgnoreCase("COUPLE"))  {
                add(scores, TastingNoteTypeCode.FRUITY, 1);
                add(scores, TastingNoteTypeCode.SWEET, 1);
                add(scores, TastingNoteTypeCode.FLORAL, 1);
            } else if (key.equalsIgnoreCase("FAMILY" )) {
                add(scores, TastingNoteTypeCode.EARTHY, 1);
                add(scores, TastingNoteTypeCode.SWEET, 1);
                add(scores, TastingNoteTypeCode.OCEANIC, 1);
            } else if (key.equalsIgnoreCase("ANYONE")) {
                add(scores, TastingNoteTypeCode.SPICES, 1);
                add(scores, TastingNoteTypeCode.FLORAL, 1);
                add(scores, TastingNoteTypeCode.NUTTY, 1);
            }
        }
    }

    private static void add(Map<TastingNoteTypeCode, Integer> scores, TastingNoteTypeCode type, int delta) {
        scores.put(type, scores.get(type) + delta);
    }
}