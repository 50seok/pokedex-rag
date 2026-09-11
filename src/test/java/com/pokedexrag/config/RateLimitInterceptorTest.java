package com.pokedexrag.config;

import com.pokedexrag.exception.CustomException;
import com.pokedexrag.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private final RateLimitInterceptor interceptor = new RateLimitInterceptor();

    @Test
    void preHandle_allowsUpToLimitPerMinuteThenBlocksTheNextRequest() {
        given(request.getRemoteAddr()).willReturn("1.2.3.4");

        for (int i = 0; i < 20; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED));
    }

    @Test
    void preHandle_tracksEachIpSeparately() {
        given(request.getRemoteAddr()).willReturn("1.1.1.1");
        for (int i = 0; i < 20; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(CustomException.class);

        given(request.getRemoteAddr()).willReturn("2.2.2.2");

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    void preHandle_prefersXForwardedForOverRemoteAddr() {
        given(request.getHeader("X-Forwarded-For")).willReturn("203.0.113.5, 10.0.0.1");
        for (int i = 0; i < 20; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED));
    }
}
