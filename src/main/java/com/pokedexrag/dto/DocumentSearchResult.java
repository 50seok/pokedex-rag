package com.pokedexrag.dto;

public record DocumentSearchResult(
        long id,
        String sourceType,
        int sourceId,
        String title,
        String content,
        double distance
) {
}
