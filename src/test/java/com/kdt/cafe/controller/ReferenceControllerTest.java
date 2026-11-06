package com.kdt.cafe.controller;

import com.kdt.cafe.config.RestClientConfig;
import com.kdt.cafe.service.ReferenceService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * 선택 심화: RestClient 외부 조회·실패 처리 테스트
 * - 실제 외부 서버(jsonplaceholder) 대신 테스트 안에서 모의 외부 서버를 띄우고,
 *   @DynamicPropertySource로 cafe.outside.base-url을 그 주소로 바꿔서 실제 RestClient가 호출하게 함
 * - 우리 API는 MockMvc로 호출: Controller → Service → 실제 RestClient(타임아웃 3초·5초) → 모의 서버
 * - 사례마다 필요한 외부 주소가 달라서 @Nested 클래스로 나눔 (클래스마다 base-url이 다름)
 *
 * 실행: ./gradlew test --tests '*ReferenceControllerTest'
 */
@WebMvcTest(ReferenceController.class)
@Import({RestClientConfig.class, ReferenceService.class})
@ImportAutoConfiguration(RestClientAutoConfiguration.class) // @WebMvcTest에는 RestClient.Builder 자동 설정이 없어서 추가
class ReferenceControllerTest {

    @Autowired
    MockMvc mockMvc;

    // 공통 오류 형식: 상태·code·message와 fieldErrors={}
    private void assertError(ResultActions result, int status, String code, String message) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").value(message))
                // isEmpty()만으로는 null·""·[]도 통과하므로, JSON 객체({})인지 isMap()으로 함께 확인
                .andExpect(jsonPath("$.fieldErrors").isMap())
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    // ===== 모의 외부 서버가 응답하는 사례: 성공·404·서버 오류·응답 읽기 시간 초과 =====
    @Nested
    class MockServerResponds {

        // postId에 따라 모의 서버의 응답을 정함
        static final int NOT_FOUND_ID = 404;
        static final int SERVER_ERROR_ID = 500;
        static final int SLOW_ID = 999; // 읽기 타임아웃(5초)보다 늦게 응답

        static HttpServer mockServer;

        @DynamicPropertySource
        static void outsideUrl(DynamicPropertyRegistry registry) throws IOException {
            mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0); // 0: 비어 있는 포트를 OS가 고름
            mockServer.createContext("/posts/", MockServerResponds::handle);
            mockServer.setExecutor(Executors.newCachedThreadPool()); // 느린 응답이 다음 요청을 막지 않도록
            mockServer.start();
            registry.add("cafe.outside.base-url", () -> "http://127.0.0.1:" + mockServer.getAddress().getPort());
        }

        @AfterAll
        static void stopServer() {
            mockServer.stop(0);
        }

        private static void handle(HttpExchange exchange) throws IOException {
            int postId = Integer.parseInt(exchange.getRequestURI().getPath().substring("/posts/".length()));
            switch (postId) {
                case NOT_FOUND_ID -> exchange.sendResponseHeaders(404, -1);
                case SERVER_ERROR_ID -> exchange.sendResponseHeaders(500, -1);
                case SLOW_ID -> {
                    sleep(7000);
                    exchange.sendResponseHeaders(200, -1);
                }
                default -> {
                    // 요청한 postId를 그대로 담아 응답 → 입력 ID에 맞는 외부 응답을 사용하는지 확인
                    String json = """
                            {"id": %d, "userId": 7, "title": "모의 제목 %d", "body": "모의 본문 %d"}
                            """.formatted(postId, postId, postId);
                    byte[] body = json.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, body.length);
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(body);
                    }
                }
            }
            exchange.close();
        }

        @Test
        @DisplayName("외부 서버가 200이면 요청한 postId의 외부 응답 값(id·userId·title·body)을 그대로 200으로 반환한다")
        void success() throws Exception {
            mockMvc.perform(get("/api/v1/reference/posts/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.userId").value(7))
                    .andExpect(jsonPath("$.title").value("모의 제목 3"))
                    .andExpect(jsonPath("$.body").value("모의 본문 3"));
        }

        @Test
        @DisplayName("외부 서버가 404이면 404 REFERENCE_POST_NOT_FOUND이다")
        void outside_not_found() throws Exception {
            assertError(mockMvc.perform(get("/api/v1/reference/posts/" + NOT_FOUND_ID)),
                    404, "REFERENCE_POST_NOT_FOUND", "외부 게시글을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("외부 서버가 500이면 502 OUTSIDE_API_FAILED이다")
        void outside_server_error() throws Exception {
            assertError(mockMvc.perform(get("/api/v1/reference/posts/" + SERVER_ERROR_ID)),
                    502, "OUTSIDE_API_FAILED", "외부 서버를 호출하지 못했습니다.");
        }

        @Test
        @DisplayName("연결 후 외부 서버가 5초 안에 응답하지 않으면 504 OUTSIDE_API_TIMEOUT이다")
        void read_timeout() throws Exception {
            assertError(mockMvc.perform(get("/api/v1/reference/posts/" + SLOW_ID)),
                    504, "OUTSIDE_API_TIMEOUT", "외부 서버의 응답 시간이 초과되었습니다.");
        }
    }

    // ===== 연결 거절: 아무도 듣고 있지 않은 포트 =====
    @Nested
    class ConnectionRefused {

        @DynamicPropertySource
        static void outsideUrl(DynamicPropertyRegistry registry) throws IOException {
            int closedPort;
            try (ServerSocket socket = new ServerSocket(0)) { // 빈 포트를 하나 받은 뒤 바로 닫음
                closedPort = socket.getLocalPort();
            }
            registry.add("cafe.outside.base-url", () -> "http://127.0.0.1:" + closedPort);
        }

        @Test
        @DisplayName("외부 서버에 연결이 거절되면 502 OUTSIDE_API_FAILED이다")
        void connection_refused() throws Exception {
            assertError(mockMvc.perform(get("/api/v1/reference/posts/1")),
                    502, "OUTSIDE_API_FAILED", "외부 서버를 호출하지 못했습니다.");
        }
    }

    // ===== 연결 시간 초과: 연결 대기열이 꽉 차서 연결 요청에 응답하지 않는 소켓 =====
    @Nested
    class ConnectTimeout {

        static ServerSocket fullSocket;
        static final List<Socket> fillers = new ArrayList<>();

        @DynamicPropertySource
        static void outsideUrl(DynamicPropertyRegistry registry) throws IOException {
            // 대기열 크기 1로 열고 accept()를 하지 않음 → 대기열이 차면 OS가 새 연결 요청을 무시함
            fullSocket = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
            InetSocketAddress address = new InetSocketAddress(fullSocket.getInetAddress(), fullSocket.getLocalPort());

            // 연결이 더 이상 받아들여지지 않을 때까지 대기열을 채움
            for (int i = 0; i < 50; i++) {
                Socket filler = new Socket();
                try {
                    filler.connect(address, 200);
                    fillers.add(filler);
                } catch (SocketTimeoutException full) {
                    filler.close();
                    break;
                }
            }
            registry.add("cafe.outside.base-url", () -> "http://127.0.0.1:" + fullSocket.getLocalPort());
        }

        @AfterAll
        static void closeSockets() throws IOException {
            for (Socket filler : fillers) {
                filler.close();
            }
            fullSocket.close();
        }

        @Test
        @DisplayName("외부 서버와 3초 안에 연결되지 않으면 504 OUTSIDE_API_TIMEOUT이다")
        void connect_timeout() throws Exception {
            assertError(mockMvc.perform(get("/api/v1/reference/posts/1")),
                    504, "OUTSIDE_API_TIMEOUT", "외부 서버의 응답 시간이 초과되었습니다.");
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
