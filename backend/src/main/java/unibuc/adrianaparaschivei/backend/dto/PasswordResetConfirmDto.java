package unibuc.adrianaparaschivei.backend.dto;

public record PasswordResetConfirmDto(
        String email,
        String token,
        String newPassword
) {
}
