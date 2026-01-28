package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.QuickKeyword;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.Map;

//간단 진단 점수 계산용
public class QuickScoring {
    private static final Map<QuickKeyword,MainSub> QUICK_RULES = Map.of(


    );

    private record MainSub(TastingNoteTypeCode main, TastingNoteTypeCode sub){
    }
}
