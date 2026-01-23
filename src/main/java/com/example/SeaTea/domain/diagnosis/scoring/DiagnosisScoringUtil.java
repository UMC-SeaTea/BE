package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.*;

//각 문항 응답을 점수로 반환
/***공용 유틸 **/
public class DiagnosisScoringUtil {

    //step1과 step2의 점수를 합산
    public static EnumMap<TastingNoteTypeCode, Integer> merge(
            Map<TastingNoteTypeCode, Integer> a,
            Map<TastingNoteTypeCode, Integer> b
    ) { //모든 타입을 0으로 초기화한 merged 객체
        EnumMap<TastingNoteTypeCode, Integer> merged = initScores();
        //논리적으로 null 필요없지만 일단 놔둠
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

    //1등 타입 찾기(동점 포함됨)
    public static List<TastingNoteTypeCode> findTopTypes(Map<TastingNoteTypeCode, Integer> scores) {
        int max = Integer.MIN_VALUE;
        for (int v : scores.values()) max = Math.max(max, v);

        List<TastingNoteTypeCode> tops = new ArrayList<>();
        for (var e : scores.entrySet()) {
            if (e.getValue() == max) tops.add(e.getKey());
        }
        return tops;
    }

    //특정 타입의 점수 조회용
    public static int getScore(Map<TastingNoteTypeCode, Integer> scores, TastingNoteTypeCode type) {
        return scores.getOrDefault(type, 0);
    }

    //점수판 초기화
    private static EnumMap<TastingNoteTypeCode, Integer> initScores() {
        EnumMap<TastingNoteTypeCode, Integer> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode t : TastingNoteTypeCode.values()) scores.put(t, 0);
        return scores;
    }
}