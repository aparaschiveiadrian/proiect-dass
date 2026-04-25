package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import unibuc.adrianaparaschivei.backend.common.ClientIp;
import unibuc.adrianaparaschivei.backend.dto.AuthResultDto;
import unibuc.adrianaparaschivei.backend.dto.LoginRequestDto;
import unibuc.adrianaparaschivei.backend.dto.PasswordResetConfirmDto;
import unibuc.adrianaparaschivei.backend.dto.PasswordResetRequestDto;
import unibuc.adrianaparaschivei.backend.dto.UserRegisterRequestDto;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.service.AuditService;
import unibuc.adrianaparaschivei.backend.service.AuthService;
import unibuc.adrianaparaschivei.backend.service.UserService;

import java.util.Optional;

@Controller
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final AuditService auditService;

    public AuthController(AuthService authService, UserService userService, AuditService auditService) {
        this.authService = authService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password, HttpServletRequest request, Model model) {
        UserRegisterRequestDto registerRequest = new UserRegisterRequestDto(email, password);
        AuthResultDto result = authService.register(registerRequest, request);
        model.addAttribute(result.success() ? "success" : "error", result.message());
        return "register";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpServletRequest request, HttpServletResponse response, Model model) {
        LoginRequestDto loginRequest = new LoginRequestDto(email, password);
        AuthResultDto result = authService.login(loginRequest, request, response);
        if (result.success()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("error", result.message());
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return "redirect:/login?error=Logged out";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, HttpServletRequest request, Model model) {
        PasswordResetRequestDto resetRequest = new PasswordResetRequestDto(email);
        String normalizedEmail = resetRequest.email().trim().toLowerCase();
        String token = authService.predictableResetToken(normalizedEmail);
        String resetLink = "/reset-password?email=" + normalizedEmail + "&token=" + token;
        Optional<User> user = userService.findByEmail(normalizedEmail);
        auditService.log(user.orElse(null), "PASSWORD_RESET_REQUEST", "auth", normalizedEmail, ClientIp.from(request));
        model.addAttribute("resetLink", resetLink);
        model.addAttribute("message", "Vulnerable demo: reset link generated without email verification.");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String email, @RequestParam String token, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String token, @RequestParam String newPassword, HttpServletRequest request, Model model) {
        PasswordResetConfirmDto resetConfirm = new PasswordResetConfirmDto(email, token, newPassword);
        String normalizedEmail = resetConfirm.email().trim().toLowerCase();
        if (!authService.isValidPredictableResetToken(normalizedEmail, resetConfirm.token())) {
            model.addAttribute("error", "Invalid reset token");
            model.addAttribute("email", resetConfirm.email());
            model.addAttribute("token", resetConfirm.token());
            return "reset-password";
        }

        Optional<User> userOptional = userService.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "User does not exist");
            model.addAttribute("email", resetConfirm.email());
            model.addAttribute("token", resetConfirm.token());
            return "reset-password";
        }

        User user = userOptional.get();
        userService.updatePasswordVulnerable(user, resetConfirm.newPassword());
        auditService.log(user, "PASSWORD_RESET_SUCCESS", "auth", user.getId().toString(), ClientIp.from(request));
        model.addAttribute("success", "Password changed. The same token can be reused in this vulnerable version.");
        model.addAttribute("email", resetConfirm.email());
        model.addAttribute("token", resetConfirm.token());
        return "reset-password";
    }
}

