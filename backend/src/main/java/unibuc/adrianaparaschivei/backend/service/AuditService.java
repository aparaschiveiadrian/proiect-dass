package unibuc.adrianaparaschivei.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.model.AuditLog;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.AuditLogRepository;

@Service
public class AuditService {
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
}
