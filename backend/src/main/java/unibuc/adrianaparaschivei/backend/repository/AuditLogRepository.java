package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import unibuc.adrianaparaschivei.backend.model.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    @EntityGraph(attributePaths = "user")
    List<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
