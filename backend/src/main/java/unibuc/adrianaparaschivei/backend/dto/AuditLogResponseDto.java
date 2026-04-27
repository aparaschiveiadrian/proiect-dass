package unibuc.adrianaparaschivei.backend.dto;

public record AuditLogResponseDto(
        String id,
        String userId,
        String userEmail,
        String action,
        String resource,
        String resourceId,
        String timestamp,
        String ipAddress
) {
}
