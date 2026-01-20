package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.EnumMap;
import java.util.Map;

public class DiagnosisStep2 {

    /**
     * Step2(Q5~Q8) 점수 계산
     * - Q5~Q8: A/B (가중치 +1~+3)
     */
    public static Map<TastingNoteTypeCode, Integer> scoreStep2(DiagnosisSubmitRequestDTO req) {
        EnumMap<TastingNoteTypeCode, Integer> scores = initScores();

        applyQ5(scores, req.getQ5());
        applyQ6(scores, req.getQ6());
        applyQ7(scores, req.getQ7());
        applyQ8(scores, req.getQ8());

        return scores;
    }

    private static EnumMap<TastingNoteTypeCode, Integer> initScores() {
        EnumMap<TastingNoteTypeCode, Integer> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode t : TastingNoteTypeCode.values()) {
            scores.put(t, 0);
        }
        return scores;
    }

    private static void applyQ5(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.FRUITY, 2);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.SWEET, 2);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        } else if ("B".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.SMOKY, 2);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
        }
    }

    private static void applyQ6(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.SMOKY, 2);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        } else if ("B".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.SPICES, 2);
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.FLORAL, 2);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        }
    }

    private static void applyQ7(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.NUTTY, 2);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        } else if ("B".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.FLORAL, 2);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 2);
        }
    }

    private static void applyQ8(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.OCEANIC, 3);
            add(scores, TastingNoteTypeCode.FLORAL, 1);
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.SPICES, 1);
        } else if ("B".equalsIgnoreCase(answer)) {
            add(scores, TastingNoteTypeCode.EARTHY, 2);
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.SWEET, 2);
        }
    }

    private static void add(Map<TastingNoteTypeCode, Integer> scores, TastingNoteTypeCode type, int delta) {
        scores.put(type, scores.get(type) + delta);
    }
}