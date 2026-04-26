package unibuc.adrianaparaschivei.backend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.common.AuthCookieNames;
import unibuc.adrianaparaschivei.backend.common.ClientIp;
import unibuc.adrianaparaschivei.backend.common.CookieReader;
import unibuc.adrianaparaschivei.backend.dto.AuthResultDto;
import unibuc.adrianaparaschivei.backend.dto.LoginRequestDto;
import unibuc.adrianaparaschivei.backend.dto.UserRegisterRequestDto;
import unibuc.adrianaparaschivei.backend.model.User;

import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final AuditService auditService;

    public AuthService(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResultDto register(UserRegisterRequestDto registerRequest, HttpServletRequest httpRequest) {
        if (isBlank(registerRequest.email()) || isBlank(registerRequest.password())) {
            return AuthResultDto.failure("Email and password are required");
        }
        if (userService.existsByEmail(registerRequest.email())) {
            return AuthResultDto.failure("User already exists");
        }

        User user = userService.registerVulnerable(registerRequest);
        auditService.log(user, "REGISTER", "auth", user.getId().toString(), ClientIp.from(httpRequest));
        return AuthResultDto.success("Account created successfully");
    }

    @Transactional
    public AuthResultDto login(LoginRequestDto loginRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Optional<User> userOptional = userService.findByEmailUnsafe(loginRequest.email());
        if (userOptional.isEmpty()) {
            auditService.log(null, "LOGIN_FAILED", "auth", loginRequest.email(), ClientIp.from(httpRequest));
            return AuthResultDto.failure("User does not exist");
        }

        Optional<User> authenticatedUser = userService.loginUnsafe(loginRequest.email(), loginRequest.password());
        if (authenticatedUser.isEmpty()) {
            User existingUser = userOptional.get();
            auditService.log(existingUser, "LOGIN_FAILED", "auth", existingUser.getId().toString(), ClientIp.from(httpRequest));
            return AuthResultDto.failure("Wrong password");
        }

        User user = authenticatedUser.get();
        createVulnerableAuthCookie(httpResponse, user);
        auditService.log(user, "LOGIN_SUCCESS", "auth", user.getId().toString(), ClientIp.from(httpRequest));
        return AuthResultDto.success("Login successful");
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String loggedUserId = CookieReader.findCookieValue(request, AuthCookieNames.AUTH_COOKIE).orElse(null);
        auditService.log(null, "LOGOUT", "auth", loggedUserId, ClientIp.from(request));
        deleteVulnerableAuthCookie(response);
    }

    public String predictableResetToken(String email) {
        return "reset-" + normalizeEmail(email);
    }

    public boolean isValidPredictableResetToken(String email, String token) {
        return predictableResetToken(email).equals(token);
    }

    private void createVulnerableAuthCookie(HttpServletResponse response, User user) {
        Cookie cookie = new Cookie(AuthCookieNames.AUTH_COOKIE, user.getId().toString());
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);
    }

    private void deleteVulnerableAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(AuthCookieNames.AUTH_COOKIE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
