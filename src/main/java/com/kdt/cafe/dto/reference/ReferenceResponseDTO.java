package com.kdt.cafe.dto.reference;

public record ReferenceResponseDTO(
        int id,
        int userId,
        String title,
        String body
) {
}
