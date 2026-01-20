package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.*;

public class DiagnosisScoringUtil {

    public static EnumMap<TastingNoteTypeCode, Integer> merge(
            Map<TastingNoteTypeCode, Integer> a,
            Map<TastingNoteTypeCode, Integer> b
    ) {
        EnumMap<TastingNoteTypeCode, Integer> merged = initScores();

        if (a != null) {
            for (var e : a.entrySet()) {
                merged.put(e.getKey(), merged.get(e.getKey()) + e.getValue());
            }
        }
        if (b != null) {
            for (var e : b.entrySet()) {
                merged.put(e.getKey(), merged.get(e.getKey()) + e.getValue());
            }
        }
        return merged;
    }

    public static List<TastingNoteTypeCode> findTopTypes(Map<TastingNoteTypeCode, Integer> scores) {
        int max = Integer.MIN_VALUE;
        for (int v : scores.values()) max = Math.max(max, v);

        List<TastingNoteTypeCode> tops = new ArrayList<>();
        for (var e : scores.entrySet()) {
            if (e.getValue() == max) tops.add(e.getKey());
        }
        return tops;
    }

    public static int getScore(Map<TastingNoteTypeCode, Integer> scores, TastingNoteTypeCode type) {
        return scores.getOrDefault(type, 0);
    }

    private static EnumMap<TastingNoteTypeCode, Integer> initScores() {
        EnumMap<TastingNoteTypeCode, Integer> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode t : TastingNoteTypeCode.values()) scores.put(t, 0);
        return scores;
    }
}