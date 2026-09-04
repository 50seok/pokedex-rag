package com.pokedexrag.dto;

import java.util.List;

public record ChatResponse(String answer, List<SourceDto> sources) {

    public static ChatResponse of(String answer, List<DocumentSearchResult> results) {
        return new ChatResponse(answer, results.stream().map(SourceDto::from).toList());
    }
}
