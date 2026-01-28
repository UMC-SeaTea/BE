package com.example.SeaTea.domain.diagnosis.scoring;

import com.example.SeaTea.domain.diagnosis.enums.QuickKeyword;
import com.example.SeaTea.domain.diagnosis.enums.TastingNoteTypeCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class QuickScoring {

    private static final Map<QuickKeyword, MainSub> QUICK_RULES = Map.ofEntries(
            entry(QuickKeyword.SENSUAL,       new MainSub(TastingNoteTypeCode.FLORAL,  TastingNoteTypeCode.SPICES)),
            entry(QuickKeyword.SOFT,          new MainSub(TastingNoteTypeCode.NUTTY,   TastingNoteTypeCode.SMOKY)),
            entry(QuickKeyword.SUGAR_RUSH,    new MainSub(TastingNoteTypeCode.SWEET,   TastingNoteTypeCode.FRUITY)),
            entry(QuickKeyword.TRANQUIL,      new MainSub(TastingNoteTypeCode.SMOKY,   TastingNoteTypeCode.EARTHY)),
            entry(QuickKeyword.HORIZON,       new MainSub(TastingNoteTypeCode.OCEANIC, TastingNoteTypeCode.FLORAL)),
            entry(QuickKeyword.ORGANIC,       new MainSub(TastingNoteTypeCode.EARTHY,  TastingNoteTypeCode.NUTTY)),
            entry(QuickKeyword.EXPERIMENTAL,  new MainSub(TastingNoteTypeCode.SPICES,  TastingNoteTypeCode.FLORAL)),
            entry(QuickKeyword.TASTE_RESPECT, new MainSub(TastingNoteTypeCode.NUTTY,   TastingNoteTypeCode.SMOKY)),
            entry(QuickKeyword.COOL,          new MainSub(TastingNoteTypeCode.OCEANIC, TastingNoteTypeCode.EARTHY)),
            entry(QuickKeyword.SWEET_SOUR,    new MainSub(TastingNoteTypeCode.SWEET,   TastingNoteTypeCode.SPICES)),
            entry(QuickKeyword.HIGH_TENSION,  new MainSub(TastingNoteTypeCode.FRUITY,  TastingNoteTypeCode.SPICES)),
            entry(QuickKeyword.MEDITATIVE,    new MainSub(TastingNoteTypeCode.SMOKY,   TastingNoteTypeCode.NUTTY)),
            entry(QuickKeyword.RHYTHMICAL,    new MainSub(TastingNoteTypeCode.FRUITY,  TastingNoteTypeCode.OCEANIC)),
            entry(QuickKeyword.UNPREDICTABLE, new MainSub(TastingNoteTypeCode.SPICES,  TastingNoteTypeCode.FRUITY)),
            entry(QuickKeyword.SELF_CARE,     new MainSub(TastingNoteTypeCode.EARTHY,  TastingNoteTypeCode.SWEET)),
            entry(QuickKeyword.NEW,           new MainSub(TastingNoteTypeCode.FLORAL,  TastingNoteTypeCode.FRUITY)),
            entry(QuickKeyword.AESTHETIC,     new MainSub(TastingNoteTypeCode.FLORAL,  TastingNoteTypeCode.SWEET)),
            entry(QuickKeyword.COMFORT_ZONE,  new MainSub(TastingNoteTypeCode.SWEET,   TastingNoteTypeCode.NUTTY))
    );

    private record MainSub(TastingNoteTypeCode main, TastingNoteTypeCode sub) {}

    /** 점수 계산만 담당 */
    public static Map<TastingNoteTypeCode, Double> score(List<QuickKeyword> keywords) {
        EnumMap<TastingNoteTypeCode, Double> scores = new EnumMap<>(TastingNoteTypeCode.class);
        for (TastingNoteTypeCode type : TastingNoteTypeCode.values()) {
            scores.put(type, 0.0);
        }

        if (keywords == null) return scores;

        for (QuickKeyword keyword : keywords) {
            MainSub rule = QUICK_RULES.get(keyword);
            if (rule == null) continue;

            scores.put(rule.main(), scores.get(rule.main()) + 3);
            scores.put(rule.sub(), scores.get(rule.sub()) + 1);
        }

        return scores;
    }

    /** 동점 처리용 */
    public static TastingNoteTypeCode getMainType(QuickKeyword keyword) {
        return QUICK_RULES.get(keyword).main();
    }
}