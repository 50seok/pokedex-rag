package com.pokedexrag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * prod 프로파일에서만 {@link RateLimitInterceptor}를 /api/chat에 등록한다(이슈 #36).
 * 로컬(local 프로파일)에서는 인터셉터를 아예 등록하지 않아 완전히 무제한으로 테스트할 수 있다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Environment environment;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(Environment environment, RateLimitInterceptor rateLimitInterceptor) {
        this.environment = environment;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (environment.matchesProfiles("prod")) {
            registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/chat");
        }
    }
}
