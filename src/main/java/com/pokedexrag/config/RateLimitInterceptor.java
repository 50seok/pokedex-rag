package com.pokedexrag.config;

import com.pokedexrag.exception.CustomException;
import com.pokedexrag.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * IP당 분당 {@value #LIMIT_PER_MINUTE}건으로 요청을 제한한다(이슈 #36).
 * 인증 없는 공개 엔드포인트인 /api/chat이 스크립트로 반복 호출되어 Gemini 무료 티어
 * 쿼터가 소진되는 것을 막기 위한 최소 방어선. prod 프로파일에서만 {@link WebConfig}가 등록한다.
 *
 * <p>고정 윈도우(fixed window) 카운터 — {@code epoch millis / 60_000}을 윈도우 키로 써서
 * IP별 요청 수를 센다. 슬라이딩 윈도우 대비 윈도우 경계에서 순간적으로 최대 2배까지 허용될 수
 * 있지만, 쿼터 소진을 막는 것이 목적인 이 규모에서는 충분하다.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMIT_PER_MINUTE = 20;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // ponytail: request.getRemoteAddr()만 사용 — Render는 프록시 뒤라 X-Forwarded-For를 안 보면
        // 실제로는 프록시 IP 하나로 뭉뚱그려질 수 있다. IP별 정밀도보다 "무제한 스크립트 호출 차단"이
        // 목적인 포트폴리오 규모에서는 허용 가능한 단순화. 필요해지면 X-Forwarded-For 파싱 추가.
        String ip = request.getRemoteAddr();
        long currentWindowStart = System.currentTimeMillis() / 60_000;

        Window window = windows.compute(ip, (key, existing) ->
                (existing == null || existing.windowStart() != currentWindowStart)
                        ? new Window(currentWindowStart, 1)
                        : new Window(existing.windowStart(), existing.count() + 1));

        if (window.count() > LIMIT_PER_MINUTE) {
            throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        return true;
    }

    // ponytail: IP맵을 정리(cleanup)하지 않아 장기 운영 시 서로 다른 IP 수만큼 계속 쌓인다.
    // 업그레이드 경로: 스케줄러로 오래된 윈도우 주기적 제거, 또는 Caffeine 같은 TTL 캐시로 교체.
    private record Window(long windowStart, int count) {
    }
}
