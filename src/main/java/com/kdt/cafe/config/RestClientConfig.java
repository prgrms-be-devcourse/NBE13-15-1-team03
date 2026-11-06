package com.kdt.cafe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

// 선택 심화: 외부 게시글 조회용 RestClient 설정 (주소·타임아웃)
@Configuration
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3); // 연결 대기 최대 3초
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);    // 연결 후 응답 읽기 대기 최대 5초

    // 외부 주소는 cafe.outside.base-url (환경변수 REST_URL로 변경 가능)
    @Bean
    public RestClient referenceRestClient(RestClient.Builder builder,
                                          @Value("${cafe.outside.base-url}") String baseUrl) {
        // 실제 HTTP 연결을 만드는 객체에 타임아웃을 지정하고, RestClient가 이 객체로 요청하게 함
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
