package unibuc.adrianaparaschivei.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.exceptions.AccessForbiddenException;
import unibuc.adrianaparaschivei.backend.model.AuditLog;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.AuditLogRepository;

import java.util.List;

@Service
public class AuditService {
    private static final int AUDIT_LOG_LIMIT = 100;

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User user, String action, String resource, String resourceId, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setResource(resource);
        auditLog.setResourceId(resourceId);
        auditLog.setIpAddress(ipAddress);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> listRecentLogs(User actor) {
        if (actor.getRole() != Role.MANAGER) {
            throw new AccessForbiddenException("Only managers can view audit logs");
        }
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, AUDIT_LOG_LIMIT));
    }
}
