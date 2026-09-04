package com.pokedexrag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Gemini generateContent API로 시스템 지시문+사용자 프롬프트를 넘겨 답변 텍스트를 생성한다.
 */
@Service
public class GeminiChatService {

    private static final String GENERATE_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiChatService(RestClient.Builder restClientBuilder, @Value("${gemini.api.key}") String apiKey,
                              @Value("${gemini.chat.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = restClientBuilder.build();
    }

    public String generate(String systemInstruction, String userPrompt) {
        GenerateContentRequest request = new GenerateContentRequest(
                new SystemInstruction(List.of(new Part(systemInstruction))),
                List.of(new Content("user", List.of(new Part(userPrompt)))));

        GenerateContentResponse response = restClient.post()
                .uri(GENERATE_URL_TEMPLATE.formatted(model))
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GenerateContentResponse.class);

        if (response.candidates() == null || response.candidates().isEmpty()) {
            // 안전 필터 차단 등으로 candidates가 비어 promptFeedback만 오는 경우
            throw new IllegalStateException("Gemini 응답에 candidates가 없습니다 (안전 필터 차단 가능성)");
        }
        return response.candidates().get(0).content().parts().get(0).text();
    }

    private record GenerateContentRequest(SystemInstruction systemInstruction, List<Content> contents) {
    }

    private record SystemInstruction(List<Part> parts) {
    }

    private record Content(String role, List<Part> parts) {
    }

    private record Part(String text) {
    }

    private record GenerateContentResponse(List<Candidate> candidates) {
    }

    private record Candidate(Content content) {
    }
}
