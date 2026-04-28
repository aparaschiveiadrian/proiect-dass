package unibuc.adrianaparaschivei.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponseDto(
        UUID id,
        String title,
        String description,
        String severity,
        String status,
        UUID ownerId,
        String ownerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
