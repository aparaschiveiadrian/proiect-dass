package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import unibuc.adrianaparaschivei.backend.common.CurrentUserProvider;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.service.AuditService;

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
}
