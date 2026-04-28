package unibuc.adrianaparaschivei.backend.dto;

public record ApiErrorDto(
        boolean success,
        String message
) {
}
