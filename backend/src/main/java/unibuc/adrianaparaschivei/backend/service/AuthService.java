package unibuc.adrianaparaschivei.backend.service;

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
    private static final int AUTH_COOKIE_SECONDS = 30 * 60;
    private static final int MAX_EMAIL_LENGTH = 255;

    private final UserService userService;
    private final AuditService auditService;
    private final PasswordPolicyService passwordPolicyService;
    private final AuthTokenService authTokenService;

    public AuthService(UserService userService, AuditService auditService, PasswordPolicyService passwordPolicyService, AuthTokenService authTokenService) {
        this.userService = userService;
        this.auditService = auditService;
        this.passwordPolicyService = passwordPolicyService;
        this.authTokenService = authTokenService;
    }

    @Transactional
    public AuthResultDto register(UserRegisterRequestDto registerRequest, HttpServletRequest httpRequest) {
        if (!isValidEmail(registerRequest.email()) || isBlank(registerRequest.password())) {
            return AuthResultDto.failure("Email and password are required");
        }
        if (!passwordPolicyService.isStrongPassword(registerRequest.password())) {
            //parola nu respecta politica de securitate
            return AuthResultDto.failure(passwordPolicyService.requirementsMessage());
        }
        if (userService.existsByEmail(registerRequest.email())) {
            return AuthResultDto.failure("User already exists");
        }

        User user = userService.register(registerRequest);
        auditService.log(user, "REGISTER", "auth", user.getId().toString(), ClientIp.from(httpRequest));
        return AuthResultDto.success("Account created successfully");
    }

    @Transactional
    public AuthResultDto login(LoginRequestDto loginRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Optional<User> userOptional = userService.findByEmail(loginRequest.email());
        if (userOptional.isEmpty()) {
            userService.performDummyPasswordCheck(loginRequest.password());
            auditService.log(null, "LOGIN_FAILED", "auth", loginRequest.email(), ClientIp.from(httpRequest));
            return AuthResultDto.failure("Invalid credentials");
        }

        User user = userOptional.get();
        if (userService.isAccountLocked(user)) {
            auditService.log(user, "LOGIN_BLOCKED", "auth", user.getId().toString(), ClientIp.from(httpRequest));
            return AuthResultDto.failure("Account temporarily locked. Try again later.");
        }

        if (!userService.passwordMatches(loginRequest.password(), user)) {
            boolean accountWasLocked = userService.recordFailedLogin(user);
            auditService.log(user, "LOGIN_FAILED", "auth", user.getId().toString(), ClientIp.from(httpRequest));
            if (accountWasLocked) {
                auditService.log(user, "ACCOUNT_LOCKED", "auth", user.getId().toString(), ClientIp.from(httpRequest));
            }
            return AuthResultDto.failure("Invalid credentials");
        }

        userService.resetFailedLogins(user);
        String rawToken = authTokenService.createToken(user);
        createSecureAuthCookie(httpResponse, rawToken);
        auditService.log(user, "LOGIN_SUCCESS", "auth", user.getId().toString(), ClientIp.from(httpRequest));
        return AuthResultDto.success("Login successful");
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        CookieReader.findCookieValue(request, AuthCookieNames.AUTH_COOKIE).ifPresent(authTokenService::revokeRawToken);
        auditService.log(null, "LOGOUT", "auth", null, ClientIp.from(request));
        deleteAuthCookie(response);
    }

    private void createSecureAuthCookie(HttpServletResponse response, String rawToken) {
        response.addHeader("Set-Cookie", AuthCookieNames.AUTH_COOKIE + "=" + rawToken
                + "; Path=/"
                + "; Max-Age=" + AUTH_COOKIE_SECONDS
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=Strict");
    }

    private void deleteAuthCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", AuthCookieNames.AUTH_COOKIE + "="
                + "; Path=/"
                + "; Max-Age=0"
                + "; HttpOnly"
                + "; Secure"
                + "; SameSite=Strict");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }

        String cleanEmail = email.trim();
        if (cleanEmail.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        int atPosition = cleanEmail.indexOf('@');
        int lastDotPosition = cleanEmail.lastIndexOf('.');
        return atPosition > 0 && lastDotPosition > atPosition + 1 && lastDotPosition < cleanEmail.length() - 1;
    }
}
