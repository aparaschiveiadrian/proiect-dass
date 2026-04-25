package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import unibuc.adrianaparaschivei.backend.common.CurrentUserProvider;
import unibuc.adrianaparaschivei.backend.model.User;

@Controller
public class DashboardController {
    private final CurrentUserProvider currentUserProvider;

    public DashboardController(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        User user = currentUserProvider.from(request).orElseThrow();
        model.addAttribute("user", user);
        return "dashboard";
    }
}
