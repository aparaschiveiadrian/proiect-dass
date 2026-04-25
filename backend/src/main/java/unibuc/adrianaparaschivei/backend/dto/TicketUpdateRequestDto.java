package unibuc.adrianaparaschivei.backend.dto;

import unibuc.adrianaparaschivei.backend.model.TicketSeverity;
import unibuc.adrianaparaschivei.backend.model.TicketStatus;

public record TicketUpdateRequestDto(
        String title,
        String description,
        TicketSeverity severity,
        TicketStatus status
) {
}
