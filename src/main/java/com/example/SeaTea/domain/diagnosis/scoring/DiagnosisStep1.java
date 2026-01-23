package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

//DTO를 받아서 점수로 변환
public class DiagnosisStep1 {

    /**
     * Step1(Q1~Q4) 점수 계산
     * - Q1: A/B (각 타입 +1)
     * - Q2: A/B (각 타입 +1)
     * - Q3: 0~100 에너지
     * - Q4: 1~2개 선택 (각 선택지마다 해당 타입 +1)
     */
    //step1의 누적 점수를 담는 Map<타입,점수>을 반환
    public static Map<TastingNoteTypeCode, Integer> scoreStep1(
            String q1,
            String q2,
            Integer q3,
            List<String> q4
    ) {//step1은 복수응답과 다이얼 및 복구 때문에 파라미터를 쪼갬
        EnumMap<TastingNoteTypeCode, Integer> scores = initScores();

        applyQ1(scores, q1); //Q1 응답을 점수에 반영
        applyQ2(scores, q2); //Q2 응답을 점수에 반영
        if (q3 != null) { //Q3 응답이 있을 경우만 에너지 점수 반영
            applyQ3Energy(scores, q3);
        } //0 == null이 아님!
        applyQ4(scores, q4); //Q4 응답을 점수에 반영

        return scores; //최종 점수 Map 반환
    }

    //모든 타입코드를 key로 가지는 EnumMap 생성
    private static EnumMap<TastingNoteTypeCode, Integer> initScores() {
        EnumMap<TastingNoteTypeCode, Integer> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode t : TastingNoteTypeCode.values()) {
            scores.put(t, 0);
        } //초기 점수는 모두 0
        return scores;
    }

    /** 질문 메서드 **/
    // Q1. 지금 휴식이 필요한 이유
    private static void applyQ1(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) { //A를 선택
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        } else if ("B".equalsIgnoreCase(answer)) { //B를 선택
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.FLORAL, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        }
    }

    // Q2. 가장 최근 휴식 모습
    private static void applyQ2(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) { //A를 선택
            add(scores, TastingNoteTypeCode.SMOKY, 1);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        } else if ("B".equalsIgnoreCase(answer)) { //B를 선택
            add(scores, TastingNoteTypeCode.FRUITY, 1);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
        }
    }

    // Q3. 에너지 레벨 (0~100)
    private static void applyQ3Energy(Map<TastingNoteTypeCode, Integer> scores, int energy) {

        if (energy <= 30) {// 낮은 에너지(0~30)
            add(scores, TastingNoteTypeCode.SMOKY, 2);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
        } else if (energy <= 60) {  // 중간(31~60)
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
            add(scores, TastingNoteTypeCode.SWEET, 1);
            add(scores, TastingNoteTypeCode.FLORAL, 1);
        } else { // 높은 에너지(61~100)
            add(scores, TastingNoteTypeCode.FLORAL, 2);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.FRUITY, 1);
        }
    }

    // Q4. 누구와 함께 휴식? (중복 가능, 최대 2개)
    private static void applyQ4(Map<TastingNoteTypeCode, Integer> scores, List<String> answers) {
        //answers에는 ["ALONE",COUPLE"] 가능 또는 ["ALONE"]가능
        if (answers == null) return; //null일 수 없다.

        for (String a : answers) { //복수 선택이므로 하나씩
            if (a == null) continue; //문자열 1개면 한번, 2개 선택이면 2번 가산

            // 프론트에서 값은 "ALONE", "COUPLE", "FAMILY", "ANYONE" 같은 코드로 보내는 걸 추천.
            String key = a.trim(); //문자열 정리

            if (key.equalsIgnoreCase("ALONE")) { //혼자서
                add(scores, TastingNoteTypeCode.SMOKY, 1);
                add(scores, TastingNoteTypeCode.NUTTY, 1);
                add(scores, TastingNoteTypeCode.OCEANIC, 1);
            } else if (key.equalsIgnoreCase("COUPLE"))  { //친구 또는 연인과 함께
                add(scores, TastingNoteTypeCode.FRUITY, 1);
                add(scores, TastingNoteTypeCode.SWEET, 1);
                add(scores, TastingNoteTypeCode.FLORAL, 1);
            } else if (key.equalsIgnoreCase("FAMILY" )) { //가족들과 함께
                add(scores, TastingNoteTypeCode.EARTHY, 1);
                add(scores, TastingNoteTypeCode.SWEET, 1);
                add(scores, TastingNoteTypeCode.OCEANIC, 1);
            } else if (key.equalsIgnoreCase("ANYONE")) { //누구와도 상관 없다
                add(scores, TastingNoteTypeCode.SPICES, 1);
                add(scores, TastingNoteTypeCode.FLORAL, 1);
                add(scores, TastingNoteTypeCode.NUTTY, 1);
            }
        }
    }

    //점수를 더하는 매서드
    private static void add(Map<TastingNoteTypeCode, Integer> scores, TastingNoteTypeCode type, int delta) {
        scores.put(type, scores.get(type) + delta);
    }
}