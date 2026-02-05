package com.example.SeaTea.domain.diagnosis.converter;

import com.example.SeaTea.domain.diagnosis.exception.DiagnosisException;
import com.example.SeaTea.domain.diagnosis.exception.DiagnosisErrorStatus;
import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisDetailRequestDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiagnosisDetailConverter {

    /** Step2 처리 시 DB에 저장된 Step1(Q1~Q4) 응답을 복원하기 위한 DTO */
    public record Step1Answers(String q1, String q2, Integer q3, List<String> q4) {}

    public static List<DiagnosisResponse> fromStep1(
            DiagnosisSession session, //엔터티
            DiagnosisDetailRequestDTO req //DTO
    ) { //응답 리스트 생성
        List<DiagnosisResponse> responses = new ArrayList<>();

        add(responses, session, "Q1", req.getQ1());
        add(responses, session, "Q2", req.getQ2());
        add(responses, session, "Q3", String.valueOf(req.getQ3()));

        // Q4: 다중 선택 → 단일 코드
        if (req.getQ4() != null && !req.getQ4().isEmpty()) {
            String mergedQ4 = mergeQ4Answers(req.getQ4());
            add(responses, session, "Q4", mergedQ4);
        }

        return responses;
    }

    //step2 요청값을 엔터티로 변환
    public static List<DiagnosisResponse> fromStep2(
            DiagnosisSession session, //엔터티
            DiagnosisDetailRequestDTO req //DTO
    ) {
        List<DiagnosisResponse> responses = new ArrayList<>();

        add(responses, session, "Q5", req.getQ5());
        add(responses, session, "Q6", req.getQ6());
        add(responses, session, "Q7", req.getQ7());
        add(responses, session, "Q8", req.getQ8());

        return responses;
    }

    /**
     * Step2 요청에는 q1~q4가 포함되지 않으므로, 세션에 저장된 Step1(Q1~Q4) 응답을 DB에서 읽어 복원한다.
     * Q4는 저장 시 "ALONE_ANYONE" 처럼 '_'로 merge 되었으므로 split("_")로 되돌린다.
     */
    public static Step1Answers restoreStep1Answers(List<DiagnosisResponse> responses) {
        String q1 = null;
        String q2 = null;
        Integer q3 = null;
        List<String> q4 = null;

        for (DiagnosisResponse r : responses) {
            if (r.getItemCode() == null) continue;

            switch (r.getItemCode()) {
                case "Q1" -> q1 = r.getAnswerCode();
                case "Q2" -> q2 = r.getAnswerCode();
                case "Q3" -> {
                    if (r.getAnswerCode() != null) {
                        q3 = Integer.parseInt(r.getAnswerCode());
                    }
                }
                case "Q4" -> {
                    if (r.getAnswerCode() != null) {
                        q4 = Arrays.asList(r.getAnswerCode().split("_"));
                    }
                }
                default -> {
                    // ignore
                }
            }
        }

        // Step1 응답 누락은 데이터 정합성 문제이므로 요청 오류로 처리
        if (q1 == null || q2 == null || q3 == null || q4 == null || q4.isEmpty()) {
            throw new DiagnosisException(DiagnosisErrorStatus._INVALID_STEP);
        } //즉, step2요청이 들어왔을 때, DB에 step1 응답이 하나라도 누락되어 있으면 예외발생.

        return new Step1Answers(q1, q2, q3, q4);
    }

    //Q4의 응답을 하나의 row에 담기 위함.
    private static String mergeQ4Answers(List<String> answers) {
        if (answers.size() == 1) {
            return answers.get(0);
        }

        return answers.stream()
                .sorted()
                .reduce((a, b) -> a + "_" + b)
                .orElseThrow();
    }

    private static void add(
            List<DiagnosisResponse> list,
            DiagnosisSession session,
            String itemCode,
            String answerCode
    ) {
        if (answerCode == null) return;

        list.add(DiagnosisResponse.builder() //엔터티 생성
                .session(session)
                .itemCode(itemCode)
                .answerCode(answerCode)
                .build());
    }
}
