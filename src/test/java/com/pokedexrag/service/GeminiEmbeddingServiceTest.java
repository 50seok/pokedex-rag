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

class GeminiEmbeddingServiceTest {

    private static final String EMBED_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    @Test
    void embed_sendsExpectedRequestAndParsesResponseInto768Floats() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String expectedRequestBody = """
                {"model":"models/gemini-embedding-001","content":{"parts":[{"text":"이상해씨는 어떤 포켓몬이야?"}]},"embedContentConfig":{"outputDimensionality":768}}""";
        String responseBody = "{\"embedding\":{\"values\":" + fakeValuesJson(768) + "}}";

        server.expect(requestTo(EMBED_URL))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(content().json(expectedRequestBody, JsonCompareMode.LENIENT))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        GeminiEmbeddingService service = new GeminiEmbeddingService(builder, "test-key");

        float[] result = service.embed("이상해씨는 어떤 포켓몬이야?");

        assertThat(result).hasSize(768);
        assertThat(result[0]).isEqualTo(0.1f);
        assertThat(result[767]).isEqualTo(0.2f);
        server.verify();
    }

    private String fakeValuesJson(int count) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(i == count - 1 ? "0.2" : "0.1");
        }
        return sb.append("]").toString();
    }
}
