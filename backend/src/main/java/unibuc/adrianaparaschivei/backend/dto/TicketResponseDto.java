package unibuc.adrianaparaschivei.backend.dto;

public record TicketResponseDto(
        String id,
        String title,
        String description,
        String severity,
        String status,
        String ownerId,
        String ownerEmail,
        String createdAt,
        String updatedAt
) {
}
