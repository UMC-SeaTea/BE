package com.example.SeaTea.domain.diagnosis.converter;

import com.example.SeaTea.domain.diagnosis.dto.request.DiagnosisSubmitRequestDTO;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;
import com.example.SeaTea.domain.diagnosis.entity.DiagnosisSession;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisResponseConverter {

    //step1 요청값을 엔터티로 변환
    public static List<DiagnosisResponse> fromStep1(
            DiagnosisSession session, //엔터티
            DiagnosisSubmitRequestDTO req //DTO
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
            DiagnosisSubmitRequestDTO req //DTO
    ) {
        List<DiagnosisResponse> responses = new ArrayList<>();

        add(responses, session, "Q5", req.getQ5());
        add(responses, session, "Q6", req.getQ6());
        add(responses, session, "Q7", req.getQ7());
        add(responses, session, "Q8", req.getQ8());

        return responses;
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
