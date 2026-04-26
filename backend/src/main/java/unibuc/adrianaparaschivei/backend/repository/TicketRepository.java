package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.domain.Pageable;
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
    List<Ticket> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    List<Ticket> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
