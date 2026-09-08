package com.pokedexrag.dto;

public record SourceDto(String type, int id, String title, String imageUrl) {

    public static SourceDto from(DocumentSearchResult result, String imageUrl) {
        return new SourceDto(result.sourceType(), result.sourceId(), result.title(), imageUrl);
    }
}
