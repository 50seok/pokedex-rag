package com.pokedexrag.service;

import com.pokedexrag.dto.ChatResponse;
import com.pokedexrag.dto.DocumentSearchResult;
import com.pokedexrag.exception.CustomException;
import com.pokedexrag.exception.ErrorCode;
import com.pokedexrag.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 질문 임베딩 -> 문서 검색 -> LLM 답변 생성을 오케스트레이션한다.
 */
@Service
public class ChatService {

    private static final int TOP_K = 5;

    private static final String SYSTEM_INSTRUCTION = """
            당신은 관동 지방 포켓몬 도감 챗봇입니다. 아래 주어진 문서에 근거해서만 답변하세요.
            문서에 없는 내용은 "정보가 없습니다"라고 답하세요.""";

    private final GeminiEmbeddingService embeddingService;
    private final DocumentRepository documentRepository;
    private final GeminiChatService geminiChatService;

    public ChatService(GeminiEmbeddingService embeddingService, DocumentRepository documentRepository,
                        GeminiChatService geminiChatService) {
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
        this.geminiChatService = geminiChatService;
    }

    public ChatResponse answer(String question) {
        float[] queryEmbedding = embeddingService.embed(question);
        List<DocumentSearchResult> results = documentRepository.searchTopK(queryEmbedding, TOP_K);

        String userPrompt = buildPrompt(question, results);

        String answer;
        try {
            answer = geminiChatService.generate(SYSTEM_INSTRUCTION, userPrompt);
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.CHAT_GENERATION_FAILED);
        }

        return ChatResponse.of(answer, results);
    }

    private String buildPrompt(String question, List<DocumentSearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("[참고 문서]\n");
        for (int i = 0; i < results.size(); i++) {
            DocumentSearchResult r = results.get(i);
            sb.append(i + 1).append(". ").append(r.title()).append("\n").append(r.content()).append("\n\n");
        }
        sb.append("[질문]\n").append(question);
        return sb.toString();
    }
}
