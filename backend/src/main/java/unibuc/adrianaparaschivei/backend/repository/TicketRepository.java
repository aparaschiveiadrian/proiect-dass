package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import unibuc.adrianaparaschivei.backend.model.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @EntityGraph(attributePaths = "owner")
    Optional<Ticket> findById(UUID id);

    @EntityGraph(attributePaths = "owner")
    List<Ticket> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "owner")
    List<Ticket> findByOwnerId(UUID ownerId);

    List<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
