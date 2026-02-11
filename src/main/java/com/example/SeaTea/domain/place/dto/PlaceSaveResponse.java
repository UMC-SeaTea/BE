package com.example.SeaTea.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSaveResponse {
    private boolean isSaved;

    public static PlaceSaveResponse saved() {
        return new PlaceSaveResponse(true);
    }

    public static PlaceSaveResponse unsaved() {
        return new PlaceSaveResponse(false);
    }
}
