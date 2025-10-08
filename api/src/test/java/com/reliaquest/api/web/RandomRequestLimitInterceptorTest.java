package com.reliaquest.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RandomRequestLimitInterceptorTest {

    private RandomRequestLimitInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        interceptor = new RandomRequestLimitInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
    }

    @Test
    @DisplayName("Should allow requests when under the limit")
    void shouldAllowRequestsUnderLimit() throws Exception {
        // Given - fresh interceptor with no previous requests

        // When - making a request
        boolean result = interceptor.preHandle(request, response, handler);

        // Then - request should be allowed
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should allow multiple requests up to the limit")
    void shouldAllowMultipleRequestsUpToLimit() throws Exception {
        // Given - interceptor with known request limit
        int requestLimit = getRequestLimit();

        // When - making requests up to the limit
        for (int i = 0; i < requestLimit; i++) {
            boolean result = interceptor.preHandle(request, response, handler);
            assertThat(result).isTrue();
        }

        // Then - all requests should be allowed
        // This is verified in the loop above
    }

    @Test
    @DisplayName("Should reject requests when limit is exceeded")
    void shouldRejectRequestsWhenLimitExceeded() throws Exception {
        // Given - interceptor that has reached its limit
        int requestLimit = getRequestLimit();

        // Exhaust the request limit
        for (int i = 0; i < requestLimit; i++) {
            interceptor.preHandle(request, response, handler);
        }

        // When - making one more request
        boolean result = interceptor.preHandle(request, response, handler);

        // Then - request should be rejected with TOO_MANY_REQUESTS status
        assertThat(result).isFalse();
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should continue rejecting requests during backoff period")
    void shouldContinueRejectingRequestsDuringBackoff() throws Exception {
        // Given - interceptor that has reached its limit
        int requestLimit = getRequestLimit();

        // Exhaust the request limit
        for (int i = 0; i < requestLimit; i++) {
            interceptor.preHandle(request, response, handler);
        }

        // When - making multiple requests during backoff period
        boolean firstRejection = interceptor.preHandle(request, response, handler);
        boolean secondRejection = interceptor.preHandle(request, response, handler);
        boolean thirdRejection = interceptor.preHandle(request, response, handler);

        // Then - all requests should be rejected
        assertThat(firstRejection).isFalse();
        assertThat(secondRejection).isFalse();
        assertThat(thirdRejection).isFalse();
    }

    @Test
    @DisplayName("Should handle backoff behavior correctly")
    void shouldHandleBackoffBehaviorCorrectly() throws Exception {
        // Given - interceptor that has reached its limit
        int requestLimit = getRequestLimit();

        // Exhaust the request limit
        for (int i = 0; i < requestLimit; i++) {
            interceptor.preHandle(request, response, handler);
        }

        // When - making request after limit is reached
        boolean rejectedResult = interceptor.preHandle(request, response, handler);

        // Then - request should be rejected with proper status
        assertThat(rejectedResult).isFalse();
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        // And - subsequent requests should also be rejected during backoff
        boolean stillRejected = interceptor.preHandle(request, response, handler);
        assertThat(stillRejected).isFalse();
    }

    @Test
    @DisplayName("Should handle concurrent requests safely")
    void shouldHandleConcurrentRequestsSafely() throws Exception {
        // Given - multiple threads making requests
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        // When - multiple threads make requests simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    results[index] = interceptor.preHandle(request, response, handler);
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - some requests should succeed, some may fail based on timing
        // At least one should succeed (the first one)
        boolean atLeastOneSuccess = false;
        for (boolean result : results) {
            if (result) {
                atLeastOneSuccess = true;
                break;
            }
        }
        assertThat(atLeastOneSuccess).isTrue();
    }

    @Test
    @DisplayName("Should increment request count correctly")
    void shouldIncrementRequestCountCorrectly() throws Exception {
        // Given - fresh interceptor

        // When - making several requests
        interceptor.preHandle(request, response, handler);
        interceptor.preHandle(request, response, handler);
        interceptor.preHandle(request, response, handler);

        // Then - request count should be tracked internally
        // We can't directly access the count, but we can verify behavior
        // by checking that we can still make requests up to the limit
        int requestLimit = getRequestLimit();

        // Make remaining requests up to limit
        for (int i = 3; i < requestLimit; i++) {
            boolean result = interceptor.preHandle(request, response, handler);
            assertThat(result).isTrue();
        }

        // Next request should be rejected
        boolean result = interceptor.preHandle(request, response, handler);
        assertThat(result).isFalse();
    }

    /**
     * Helper method to get the REQUEST_LIMIT using reflection
     */
    private int getRequestLimit() throws Exception {
        Field requestLimitField = RandomRequestLimitInterceptor.class.getDeclaredField("REQUEST_LIMIT");
        requestLimitField.setAccessible(true);
        return (int) requestLimitField.get(null);
    }
}
