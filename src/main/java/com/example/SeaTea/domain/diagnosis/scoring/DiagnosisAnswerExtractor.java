package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.entity.DiagnosisResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosisAnswerExtractor {

    public static Map<String, String> toAnswerMap(List<DiagnosisResponse> responses) {
        Map<String, String> map = new HashMap<>();
        for (DiagnosisResponse r : responses) {
            map.put(r.getItemCode(), r.getAnswerCode());
        }
        return map;
    }

    public static String get(Map<String, String> map, String key) {
        return map.get(key);
    }

    public static String first(List<DiagnosisResponse> responses, String itemCode) {
        return responses.stream()
                .filter(r -> itemCode.equals(r.getItemCode()))
                .map(DiagnosisResponse::getAnswerCode)
                .findFirst()
                .orElse(null);
    }

    public static List<String> list(List<DiagnosisResponse> responses, String itemCode) {
        return responses.stream()
                .filter(r -> itemCode.equals(r.getItemCode()))
                .map(DiagnosisResponse::getAnswerCode)
                .toList();
    }
}