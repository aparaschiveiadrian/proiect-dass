package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibuc.adrianaparaschivei.backend.model.Ticket;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByOwnerId(UUID ownerId);
    List<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
