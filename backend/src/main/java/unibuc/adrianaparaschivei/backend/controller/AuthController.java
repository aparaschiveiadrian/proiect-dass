package unibuc.adrianaparaschivei.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import unibuc.adrianaparaschivei.backend.dto.AuthResultDto;
import unibuc.adrianaparaschivei.backend.dto.LoginRequestDto;
import unibuc.adrianaparaschivei.backend.dto.PasswordResetConfirmDto;
import unibuc.adrianaparaschivei.backend.dto.PasswordResetLinkResponseDto;
import unibuc.adrianaparaschivei.backend.dto.PasswordResetRequestDto;
import unibuc.adrianaparaschivei.backend.dto.UserRegisterRequestDto;
import unibuc.adrianaparaschivei.backend.service.AuthService;
import unibuc.adrianaparaschivei.backend.service.PasswordResetService;

import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
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

    @ResponseBody
    @PostMapping("/api/register")
    public AuthResultDto apiRegister(@RequestBody UserRegisterRequestDto registerRequest, HttpServletRequest request) {
        return authService.register(registerRequest, request);
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

    @ResponseBody
    @PostMapping("/api/login")
    public AuthResultDto apiLogin(@RequestBody LoginRequestDto loginRequest, HttpServletRequest request, HttpServletResponse response) {
        return authService.login(loginRequest, request, response);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return "redirect:/login?error=Logged out";
    }

    @ResponseBody
    @PostMapping("/api/logout")
    public AuthResultDto apiLogout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return AuthResultDto.success("Logged out");
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, HttpServletRequest request, Model model) {
        Optional<String> resetLink = passwordResetService.createResetLink(email, request);
        resetLink.ifPresent(link -> model.addAttribute("resetLink", link));
        model.addAttribute("message", "If the account exists, a reset link was generated. For the local demo it is displayed here.");
        return "forgot-password";
    }

    @ResponseBody
    @PostMapping("/api/forgot-password")
    public PasswordResetLinkResponseDto apiForgotPassword(@RequestBody PasswordResetRequestDto resetRequest, HttpServletRequest request) {
        Optional<String> resetLink = passwordResetService.createResetLink(resetRequest.email(), request);
        return new PasswordResetLinkResponseDto(
                resetRequest.email(),
                "If the account exists, a reset link was generated. For the local demo it is displayed here.",
                resetLink.orElse(null)
        );
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
        AuthResultDto result = passwordResetService.resetPassword(resetConfirm, request);
        model.addAttribute(result.success() ? "success" : "error", result.message());
        model.addAttribute("email", resetConfirm.email());
        model.addAttribute("token", resetConfirm.token());
        return "reset-password";
    }

    @ResponseBody
    @PostMapping("/api/reset-password")
    public AuthResultDto apiResetPassword(@RequestBody PasswordResetConfirmDto resetConfirm, HttpServletRequest request) {
        return passwordResetService.resetPassword(resetConfirm, request);
    }

    @ResponseBody
    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        );
    }
}
