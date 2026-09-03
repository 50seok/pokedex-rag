package com.pokedexrag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Gemini embedContent API로 텍스트를 768차원 임베딩 벡터로 변환한다.
 */
@Service
public class GeminiEmbeddingService {

    private static final String EMBED_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";
    private static final String MODEL = "models/gemini-embedding-001";
    private static final int OUTPUT_DIMENSIONALITY = 768;

    private final RestClient restClient;
    private final String apiKey;

    public GeminiEmbeddingService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    public float[] embed(String text) {
        EmbedRequest request = new EmbedRequest(MODEL, new Content(List.of(new Part(text))),
                new EmbedContentConfig(OUTPUT_DIMENSIONALITY));

        EmbedResponse response = restClient.post()
                .uri(EMBED_URL)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(EmbedResponse.class);

        List<Float> values = response.embedding().values();
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private record EmbedRequest(String model, Content content, EmbedContentConfig embedContentConfig) {
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }

    private record EmbedContentConfig(int outputDimensionality) {
    }

    private record EmbedResponse(Embedding embedding) {
    }

    private record Embedding(List<Float> values) {
    }
}
