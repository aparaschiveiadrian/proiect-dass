package unibuc.adrianaparaschivei.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.common.ClientIp;
import unibuc.adrianaparaschivei.backend.dto.AuthResultDto;
import unibuc.adrianaparaschivei.backend.dto.PasswordResetConfirmDto;
import unibuc.adrianaparaschivei.backend.model.PasswordResetToken;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {
    private static final int RESET_TOKEN_MINUTES = 15;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SecureTokenService secureTokenService;
    private final UserService userService;
    private final PasswordPolicyService passwordPolicyService;
    private final AuditService auditService;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository, SecureTokenService secureTokenService, UserService userService, PasswordPolicyService passwordPolicyService, AuditService auditService) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.secureTokenService = secureTokenService;
        this.userService = userService;
        this.passwordPolicyService = passwordPolicyService;
        this.auditService = auditService;
    }

    @Transactional
    public Optional<String> createResetLink(String email, HttpServletRequest request) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            auditService.log(null, "PASSWORD_RESET_REQUEST", "auth", email, ClientIp.from(request));
            return Optional.empty();
        }

        User user = userOptional.get();
        String rawToken = secureTokenService.generateRawToken();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(secureTokenService.hashToken(rawToken));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        auditService.log(user, "PASSWORD_RESET_REQUEST", "auth", user.getId().toString(), ClientIp.from(request));
        return Optional.of("/reset-password?email=" + user.getEmail() + "&token=" + rawToken);
    }

    @Transactional
    public AuthResultDto resetPassword(PasswordResetConfirmDto request, HttpServletRequest httpRequest) {
        if (!passwordPolicyService.isStrongPassword(request.newPassword())) {
            return AuthResultDto.failure(passwordPolicyService.requirementsMessage());
        }

        String tokenHash = secureTokenService.hashToken(request.token());
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (tokenOptional.isEmpty()) {
            auditService.log(null, "PASSWORD_RESET_FAILED", "auth", request.email(), ClientIp.from(httpRequest));
            return AuthResultDto.failure("Invalid or expired reset token");
        }

        PasswordResetToken token = tokenOptional.get();
        boolean invalidToken = token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now());
        boolean emailMismatch = !token.getUser().getEmail().equalsIgnoreCase(request.email());
        if (invalidToken || emailMismatch) {
            auditService.log(token.getUser(), "PASSWORD_RESET_FAILED", "auth", token.getUser().getId().toString(), ClientIp.from(httpRequest));
            return AuthResultDto.failure("Invalid or expired reset token");
        }

        User user = token.getUser();
        userService.updatePassword(user, request.newPassword());
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        auditService.log(user, "PASSWORD_RESET_SUCCESS", "auth", user.getId().toString(), ClientIp.from(httpRequest));
        return AuthResultDto.success("Password changed successfully. The reset token is now invalid.");
    }
}
