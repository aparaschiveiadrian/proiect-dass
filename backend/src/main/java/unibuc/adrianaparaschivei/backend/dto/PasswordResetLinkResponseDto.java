package unibuc.adrianaparaschivei.backend.dto;

public record PasswordResetLinkResponseDto(
        String email,
        String message,
        String resetLink
) {
}
