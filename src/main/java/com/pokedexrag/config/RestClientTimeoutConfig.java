package com.pokedexrag.config;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

/**
 * 외부 API(Gemini 등) 호출용 RestClient에 connect/read 타임아웃을 강제한다.
 * 미설정 시 응답 없는 외부 API가 요청 스레드를 무기한 블로킹할 수 있다.
 */
@Configuration
public class RestClientTimeoutConfig {

    @Bean
    RestClientCustomizer restClientTimeoutCustomizer() {
        return builder -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofSeconds(5));
            requestFactory.setReadTimeout(Duration.ofSeconds(30));
            builder.requestFactory(requestFactory);
        };
    }
}
