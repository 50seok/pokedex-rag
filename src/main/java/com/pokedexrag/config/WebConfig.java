package com.pokedexrag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * local 프로파일이 아닐 때 {@link RateLimitInterceptor}를 /api/chat에 등록한다(이슈 #36).
 * fail-closed — "prod일 때만 등록"이 아니라 "local일 때만 제외"라서, 프로파일이 미설정이거나
 * 오타(예: SPRING_PROFILES_ACTIVE 값 실수)여도 기본적으로 rate limit이 걸린다.
 * 로컬(local 프로파일)에서만 인터셉터를 아예 등록하지 않아 완전히 무제한으로 테스트할 수 있다.
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
        if (!environment.matchesProfiles("local")) {
            registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/chat");
        }
    }
}
