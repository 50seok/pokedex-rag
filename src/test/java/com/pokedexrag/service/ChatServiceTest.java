package com.pokedexrag.service;

import com.pokedexrag.dto.ChatResponse;
import com.pokedexrag.dto.DocumentSearchResult;
import com.pokedexrag.exception.CustomException;
import com.pokedexrag.exception.ErrorCode;
import com.pokedexrag.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private GeminiEmbeddingService embeddingService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private GeminiChatService geminiChatService;

    private ChatService chatService;

    private static final float[] EMBEDDING = new float[]{0.1f, 0.2f};

    @Test
    void answer_returnsAnswerAndSourcesOnSuccess() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService);
        List<DocumentSearchResult> results = List.of(
                new DocumentSearchResult(1L, "pokemon", 25, "피카츄", "전기 타입 포켓몬입니다.", 0.1));

        given(embeddingService.embed("피카츄는 무슨 타입이야?")).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(EMBEDDING, 5)).willReturn(results);
        given(geminiChatService.generate(anyString(), anyString())).willReturn("전기 타입입니다.");

        ChatResponse response = chatService.answer("피카츄는 무슨 타입이야?");

        assertThat(response.answer()).isEqualTo("전기 타입입니다.");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).type()).isEqualTo("pokemon");
        assertThat(response.sources().get(0).id()).isEqualTo(25);
        assertThat(response.sources().get(0).title()).isEqualTo("피카츄");
        verify(documentRepository).searchTopK(any(float[].class), eq(5));
    }

    @Test
    void answer_returnsAnswerWithEmptySourcesWhenNoDocumentsFound() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService);

        given(embeddingService.embed(anyString())).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(any(float[].class), anyInt())).willReturn(List.of());
        given(geminiChatService.generate(anyString(), anyString())).willReturn("정보가 없습니다.");

        ChatResponse response = chatService.answer("아무도 모르는 질문");

        assertThat(response.answer()).isEqualTo("정보가 없습니다.");
        assertThat(response.sources()).isEmpty();
    }

    @Test
    void answer_throwsCustomExceptionWhenGenerationFails() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService);

        given(embeddingService.embed(anyString())).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(any(float[].class), anyInt())).willReturn(List.of());
        given(geminiChatService.generate(anyString(), anyString()))
                .willThrow(new IllegalStateException("safety block"));

        assertThatThrownBy(() -> chatService.answer("질문"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CHAT_GENERATION_FAILED));
    }

    @Test
    void answer_throwsCustomExceptionWhenGeminiReturnsErrorStatus() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService);

        given(embeddingService.embed(anyString())).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(any(float[].class), anyInt())).willReturn(List.of());
        given(geminiChatService.generate(anyString(), anyString()))
                .willThrow(HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests",
                        null, null, null));

        assertThatThrownBy(() -> chatService.answer("질문"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CHAT_GENERATION_FAILED));
    }

    @Test
    void answer_throwsCustomExceptionWhenGeminiConnectionFails() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService);

        given(embeddingService.embed(anyString())).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(any(float[].class), anyInt())).willReturn(List.of());
        given(geminiChatService.generate(anyString(), anyString()))
                .willThrow(new ResourceAccessException("connect timed out"));

        assertThatThrownBy(() -> chatService.answer("질문"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CHAT_GENERATION_FAILED));
    }
}
