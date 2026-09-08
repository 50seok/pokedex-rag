package com.pokedexrag.dto;

import java.util.List;

public record ChatResponse(String answer, List<SourceDto> sources) {
}
