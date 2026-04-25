package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibuc.adrianaparaschivei.backend.model.AuditLog;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
