package com.kdt.cafe.controller;

import com.kdt.cafe.dto.reference.ReferenceResponseDTO;
import com.kdt.cafe.service.ReferenceService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 선택 심화: 외부 서버의 /posts/{postId}를 RestClient로 조회
@RestController
@RequestMapping("/api/v1/reference/posts")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping("/{postId}")
    public ReferenceResponseDTO read(@PathVariable("postId") @Positive(message = "1 이상의 정수여야 합니다.") Integer postId) {
        return this.referenceService.read(postId);
    }
}
