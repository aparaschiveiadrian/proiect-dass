package unibuc.adrianaparaschivei.backend.dto;

import unibuc.adrianaparaschivei.backend.model.TicketSeverity;

public record TicketCreateRequestDto(
        String title,
        String description,
        TicketSeverity severity
) {
}
