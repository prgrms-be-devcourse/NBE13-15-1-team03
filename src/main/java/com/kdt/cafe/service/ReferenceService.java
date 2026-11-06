package com.kdt.cafe.service;

import com.kdt.cafe.dto.reference.ReferenceResponseDTO;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;

@Service
public class ReferenceService {

    private final RestClient restClient;

    // 주소·타임아웃이 설정된 RestClient를 주입받음 (config/RestClientConfig)
    public ReferenceService(RestClient referenceRestClient) {
        this.restClient = referenceRestClient;
    }

    public ReferenceResponseDTO read(int postId) {
        ReferenceResponseDTO found;
        try {
            found = this.restClient
                    .get()
                    .uri("/posts/{postId}", postId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ReferenceResponseDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            // 외부 서버 404 → 404 REFERENCE_POST_NOT_FOUND
            throw new BusinessLogicException(ErrorCode.REFERENCE_POST_NOT_FOUND);
        } catch (HttpServerErrorException e) {
            // 외부 서버 500~599 → 502 OUTSIDE_API_FAILED
            throw new BusinessLogicException(ErrorCode.OUTSIDE_API_FAILED);
        } catch (RestClientException e) {
            // 연결·응답 읽기 시간 초과 → 504 OUTSIDE_API_TIMEOUT
            // 연결 거절·도메인 오류 등 그 밖의 호출 실패 → 502 OUTSIDE_API_FAILED
            if (isTimeout(e)) {
                throw new BusinessLogicException(ErrorCode.OUTSIDE_API_TIMEOUT);
            }
            throw new BusinessLogicException(ErrorCode.OUTSIDE_API_FAILED);
        }

        if(found == null || found.title() == null){
            throw new IllegalStateException("outside response missing");
        }
        return found;
    }

    // 시간 초과는 RestClient 예외 안쪽(cause)에 SocketTimeoutException으로 담겨 있음
    // 응답 본문을 읽다가 시간이 초과되면 한 단계 더 감싸지므로 cause를 끝까지 따라가며 확인
    private boolean isTimeout(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

}
