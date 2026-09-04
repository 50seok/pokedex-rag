package com.pokedexrag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexrag.dto.ChatRequest;
import com.pokedexrag.dto.ChatResponse;
import com.pokedexrag.dto.SourceDto;
import com.pokedexrag.exception.CustomException;
import com.pokedexrag.exception.ErrorCode;
import com.pokedexrag.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ChatService chatService;

    @Test
    void chat_returns200WithBody() throws Exception {
        ChatResponse response = new ChatResponse("전기 타입입니다.",
                List.of(new SourceDto("pokemon", 25, "피카츄")));
        given(chatService.answer(anyString())).willReturn(response);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("피카츄는 무슨 타입이야?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("전기 타입입니다."))
                .andExpect(jsonPath("$.sources[0].type").value("pokemon"))
                .andExpect(jsonPath("$.sources[0].id").value(25))
                .andExpect(jsonPath("$.sources[0].title").value("피카츄"));
    }

    @Test
    void chat_returns400WhenQuestionBlank() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_returns500WhenChatServiceThrowsCustomException() throws Exception {
        given(chatService.answer(anyString())).willThrow(new CustomException(ErrorCode.CHAT_GENERATION_FAILED));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("피카츄는 무슨 타입이야?"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void chat_returns500WithGenericCodeWhenChatServiceThrowsUnexpectedException() throws Exception {
        given(chatService.answer(anyString())).willThrow(new RuntimeException("DB 커넥션 실패"));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest("피카츄는 무슨 타입이야?"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C999"));
    }

    @Test
    void chat_returns405NotSwallowedByGenericHandlerWhenWrongHttpMethod() throws Exception {
        // GlobalExceptionHandler가 annotations=RestController.class로 스코프 한정된 뒤로는(M4),
        // 경로만 맞고 핸들러 메서드가 끝내 선택되지 않는 405 케이스는 어떤 advice의 빈 타입도
        // 판별할 수 없어(handlerType=null) 우리 핸들러가 개입하지 못하고 Spring 기본 405 처리로
        // 빠진다 - 핵심 검증 포인트는 여전히 "C999로 뭉개져 500이 되지 않는다"는 것이다.
        mockMvc.perform(get("/api/chat"))
                .andExpect(status().isMethodNotAllowed());
    }
}
