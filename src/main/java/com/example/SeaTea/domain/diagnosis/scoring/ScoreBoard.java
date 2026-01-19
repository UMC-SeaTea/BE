package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ScoreBoard {

    private final EnumMap<TastingNoteTypeCode,Integer> scores =
            new EnumMap<>(TastingNoteTypeCode.class);

    public ScoreBoard() {
        for(TastingNoteTypeCode type : TastingNoteTypeCode.values()){
            scores.put(type,0); //모든 타입을 0점으로 초기화
        }
    }

    //메서드들

    public void add(TastingNoteTypeCode type, int point){
        scores.put(type,scores.get(type)+point);
    }

    public void addAll(Map<TastingNoteTypeCode,Integer> delta){
        for (Map.Entry<TastingNoteTypeCode,Integer> entry : delta.entrySet()) {
            add(entry.getKey(),entry.getValue());
        }
    }

    public int get(TastingNoteTypeCode type) {
        return scores.get(type);
    }

    public Map<TastingNoteTypeCode, Integer> snapshot() {
        return Collections.unmodifiableMap(scores);
    }

}
