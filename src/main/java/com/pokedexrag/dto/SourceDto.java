package com.pokedexrag.dto;

public record SourceDto(String type, int id, String title) {

    public static SourceDto from(DocumentSearchResult result) {
        return new SourceDto(result.sourceType(), result.sourceId(), result.title());
    }
}
