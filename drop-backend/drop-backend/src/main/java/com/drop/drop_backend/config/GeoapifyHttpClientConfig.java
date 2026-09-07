package com.drop.drop_backend.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class GeoapifyHttpClientConfig {

    // =========================================================
    // CONNECTION POOL
    // =========================================================

    private static final int MAX_TOTAL_CONNECTIONS = 50;

    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

    // =========================================================
    // TIMEOUTS
    // =========================================================

    private static final int CONNECTION_REQUEST_TIMEOUT_SECONDS = 2;

    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    private static final int RESPONSE_TIMEOUT_SECONDS = 10;

    // =========================================================
    // HTTP REQUEST FACTORY
    // =========================================================

    @Bean
    public HttpComponentsClientHttpRequestFactory geoapifyRequestFactory() {

        // =====================================================
        // CONNECTION MANAGER
        // =====================================================

        PoolingHttpClientConnectionManager connectionManager =
                PoolingHttpClientConnectionManagerBuilder
                        .create()
                        .setMaxConnTotal(
                                MAX_TOTAL_CONNECTIONS
                        )
                        .setMaxConnPerRoute(
                                MAX_CONNECTIONS_PER_ROUTE
                        )
                        .build();

        // =====================================================
        // REQUEST TIMEOUTS
        // =====================================================

        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectionRequestTimeout(
                                Timeout.ofSeconds(
                                        CONNECTION_REQUEST_TIMEOUT_SECONDS
                                )
                        )
                        .setConnectTimeout(
                                Timeout.ofSeconds(
                                        CONNECT_TIMEOUT_SECONDS
                                )
                        )
                        .setResponseTimeout(
                                Timeout.ofSeconds(
                                        RESPONSE_TIMEOUT_SECONDS
                                )
                        )
                        .build();

        // =====================================================
        // HTTP CLIENT
        // =====================================================

        CloseableHttpClient httpClient =
                HttpClients.custom()
                        .setConnectionManager(
                                connectionManager
                        )
                        .setDefaultRequestConfig(
                                requestConfig
                        )
                        .evictExpiredConnections()
                        .evictIdleConnections(
                                TimeValue.ofSeconds(30)
                        )
                        .build();

        // =====================================================
        // SPRING REQUEST FACTORY
        // =====================================================

        return new HttpComponentsClientHttpRequestFactory(
                httpClient
        );
    }
}