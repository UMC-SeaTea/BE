package com.example.SeaTea.domain.diagnosis.converter;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisDetailRequestDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisDetailConverter {

    public static List<DiagnosisResponse> fromStep1(
            DiagnosisSession session,
            DiagnosisDetailRequestDTO req
    ) {
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

    public static List<DiagnosisResponse> fromStep2(
            DiagnosisSession session,
            DiagnosisDetailRequestDTO req
    ) {
        List<DiagnosisResponse> responses = new ArrayList<>();

        add(responses, session, "Q5", req.getQ5());
        add(responses, session, "Q6", req.getQ6());
        add(responses, session, "Q7", req.getQ7());
        add(responses, session, "Q8", req.getQ8());

        return responses;
    }

    // Q4의 응답을 하나의 row에 담기 위함.
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

        list.add(DiagnosisResponse.builder()
                .session(session)
                .itemCode(itemCode)
                .answerCode(answerCode)
                .build());
    }
}
