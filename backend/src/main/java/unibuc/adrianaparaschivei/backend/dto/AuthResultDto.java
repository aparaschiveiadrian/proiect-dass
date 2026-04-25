package unibuc.adrianaparaschivei.backend.dto;

public record AuthResultDto(
        boolean success,
        String message
) {
    public static AuthResultDto success(String message) {
        return new AuthResultDto(true, message);
    }

    public static AuthResultDto failure(String message) {
        return new AuthResultDto(false, message);
    }
}
