package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisDetailRequestDTO;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.EnumMap;
import java.util.Map;

//step1으로 결정이 안되면 step2
public class DiagnosisStep2 {
    /**
     * Step2(Q5~Q8) 점수 계산
     * - Q5: A/B
     * - Q6: A/B
     * - Q7: A/B
     * - Q8: A/B
     */
    //step2의 누적 점수를 담는 Map<타입,점수>을 반환
    public static Map<TastingNoteTypeCode, Integer> scoreStep2(
            DiagnosisDetailRequestDTO req
    ) {//step2와는 다르게 그냥 DTO에서 추출
        EnumMap<TastingNoteTypeCode, Integer> scores = initScores();

        applyQ5(scores, req.getQ5()); //Q5 응답을 점수에 반영
        applyQ6(scores, req.getQ6()); //Q6 응답을 점수에 반영
        applyQ7(scores, req.getQ7()); //Q7 응답을 점수에 반영
        applyQ8(scores, req.getQ8()); //Q8 응답을 점수에 반영

        return scores; //최종 점수 Map 반환
    }

    //모든 타입코드를 Key로 가지는 EnumMap 생성
    private static EnumMap<TastingNoteTypeCode, Integer> initScores() {
        EnumMap<TastingNoteTypeCode, Integer> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode t : TastingNoteTypeCode.values()) {
            scores.put(t, 0);
        } //초기 점수는 모두 0
        return scores;
    }

    /** 질문 메서드 **/
    // Q5. 혼자 깊이 생각하는 시간이 길어질수록
    private static void applyQ5(Map<TastingNoteTypeCode, Integer> scores, String answer) {
        if (answer == null) return;

        if ("A".equalsIgnoreCase(answer)) { //A를 선택
            add(scores, TastingNoteTypeCode.FRUITY, 2);
            add(scores, TastingNoteTypeCode.SPICES, 1);
            add(scores, TastingNoteTypeCode.SWEET, 2);
            add(scores, TastingNoteTypeCode.OCEANIC, 1);
        } else if ("B".equalsIgnoreCase(answer)) { // B를 선택
            add(scores, TastingNoteTypeCode.SMOKY, 2);
            add(scores, TastingNoteTypeCode.NUTTY, 1);
            add(scores, TastingNoteTypeCode.EARTHY, 1);
        }
    }

    //Q6. 휴식 중 가장 방해되는 것은
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

    //Q7. 지금의 나에게 더 필요한 것은?
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

    //Q8. 휴식을 공간에 비유한다면
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

    //점수를 더하는 메서드
    private static void add(Map<TastingNoteTypeCode, Integer> scores, TastingNoteTypeCode type, int delta) {
        scores.put(type, scores.get(type) + delta);
    }
}