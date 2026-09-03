package com.pokedexrag.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiChatServiceTest {

    private static final String GENERATE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/test-model:generateContent";

    @Test
    void generate_sendsExpectedRequestAndParsesResponseText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String expectedRequestBody = """
                {"systemInstruction":{"parts":[{"text":"문서 기반으로만 답하라"}]},"contents":[{"role":"user","parts":[{"text":"피카츄는 어디서 잡아?"}]}]}""";
        String responseBody = """
                {"candidates":[{"content":{"parts":[{"text":"관동 지방 숲에서 잡을 수 있습니다."}]}}]}""";

        server.expect(requestTo(GENERATE_URL))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(content().json(expectedRequestBody, JsonCompareMode.LENIENT))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        GeminiChatService service = new GeminiChatService(builder, "test-key", "test-model");

        String result = service.generate("문서 기반으로만 답하라", "피카츄는 어디서 잡아?");

        assertThat(result).isEqualTo("관동 지방 숲에서 잡을 수 있습니다.");
        server.verify();
    }
}
