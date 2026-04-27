package unibuc.adrianaparaschivei.backend.dto;

public record PasswordResetLinkResponseDto(
        String message,
        String resetLink
) {
}
