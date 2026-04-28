package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import unibuc.adrianaparaschivei.backend.common.CurrentUserProvider;
import unibuc.adrianaparaschivei.backend.dto.AuditLogResponseDto;
import unibuc.adrianaparaschivei.backend.model.AuditLog;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.service.AuditService;

import java.util.List;

@Controller
public class AuditController {
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;

    public AuditController(AuditService auditService, CurrentUserProvider currentUserProvider) {
        this.auditService = auditService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/audit")
    public String auditLogs(HttpServletRequest request, Model model) {
        User actor = currentUserProvider.from(request).orElseThrow();
        model.addAttribute("logs", auditService.listRecentLogs(actor));
        model.addAttribute("user", actor);
        return "audit/list";
    }

    @ResponseBody
    @GetMapping("/api/audit")
    public List<AuditLogResponseDto> apiAuditLogs(HttpServletRequest request) {
        User actor = currentUserProvider.from(request).orElseThrow();
        return auditService.listRecentLogs(actor).stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponseDto toResponse(AuditLog auditLog) {
        User user = auditLog.getUser();
        return new AuditLogResponseDto(
                auditLog.getId(),
                user == null ? null : user.getId(),
                user == null ? null : user.getEmail(),
                auditLog.getAction(),
                auditLog.getResource(),
                auditLog.getResourceId(),
                auditLog.getTimestamp(),
                auditLog.getIpAddress()
        );
    }
}
