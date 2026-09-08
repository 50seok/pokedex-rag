package com.pokedexrag.service;

import com.pokedexrag.dto.ChatResponse;
import com.pokedexrag.dto.DocumentSearchResult;
import com.pokedexrag.entity.Pokemon;
import com.pokedexrag.exception.CustomException;
import com.pokedexrag.exception.ErrorCode;
import com.pokedexrag.repository.DocumentRepository;
import com.pokedexrag.repository.PokemonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;

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
    @Mock
    private PokemonRepository pokemonRepository;

    private ChatService chatService;

    private static final float[] EMBEDDING = new float[]{0.1f, 0.2f};

    @Test
    void answer_returnsAnswerAndSourcesOnSuccess() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService, pokemonRepository);
        List<DocumentSearchResult> results = List.of(
                new DocumentSearchResult(1L, "pokemon", 25, "피카츄", "전기 타입 포켓몬입니다.", 0.1));
        Pokemon pikachu = Pokemon.builder().id(25).spriteUrl("https://example.com/25.png").build();

        given(embeddingService.embed("피카츄는 무슨 타입이야?")).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(EMBEDDING, 5)).willReturn(results);
        given(geminiChatService.generate(anyString(), anyString())).willReturn("전기 타입입니다.");
        given(pokemonRepository.findById(25)).willReturn(Optional.of(pikachu));

        ChatResponse response = chatService.answer("피카츄는 무슨 타입이야?");

        assertThat(response.answer()).isEqualTo("전기 타입입니다.");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).type()).isEqualTo("pokemon");
        assertThat(response.sources().get(0).id()).isEqualTo(25);
        assertThat(response.sources().get(0).title()).isEqualTo("피카츄");
        assertThat(response.sources().get(0).imageUrl()).isEqualTo("https://example.com/25.png");
        verify(documentRepository).searchTopK(any(float[].class), eq(5));
    }

    @Test
    void answer_leavesImageUrlNullForNonPokemonSources() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService, pokemonRepository);
        List<DocumentSearchResult> results = List.of(
                new DocumentSearchResult(2L, "town", 1, "고동마을", "관동 지방의 첫 마을입니다.", 0.2));

        given(embeddingService.embed(anyString())).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(any(float[].class), anyInt())).willReturn(results);
        given(geminiChatService.generate(anyString(), anyString())).willReturn("고동마을입니다.");

        ChatResponse response = chatService.answer("첫 마을이 어디야?");

        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).type()).isEqualTo("town");
        assertThat(response.sources().get(0).imageUrl()).isNull();
    }

    @Test
    void answer_returnsAnswerWithEmptySourcesWhenNoDocumentsFound() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService, pokemonRepository);

        given(embeddingService.embed(anyString())).willReturn(EMBEDDING);
        given(documentRepository.searchTopK(any(float[].class), anyInt())).willReturn(List.of());
        given(geminiChatService.generate(anyString(), anyString())).willReturn("정보가 없습니다.");

        ChatResponse response = chatService.answer("아무도 모르는 질문");

        assertThat(response.answer()).isEqualTo("정보가 없습니다.");
        assertThat(response.sources()).isEmpty();
    }

    @Test
    void answer_throwsCustomExceptionWhenGenerationFails() {
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService, pokemonRepository);

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
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService, pokemonRepository);

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
        chatService = new ChatService(embeddingService, documentRepository, geminiChatService, pokemonRepository);

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
