package unibuc.adrianaparaschivei.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponseDto(
        UUID id,
        UUID userId,
        String userEmail,
        String action,
        String resource,
        String resourceId,
        LocalDateTime timestamp,
        String ipAddress
) {
}
