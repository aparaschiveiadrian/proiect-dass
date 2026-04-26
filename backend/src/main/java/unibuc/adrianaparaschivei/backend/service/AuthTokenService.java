package unibuc.adrianaparaschivei.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.model.AuthToken;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.AuthTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthTokenService {
    private static final int AUTH_TOKEN_MINUTES = 30;

    private final AuthTokenRepository authTokenRepository;
    private final SecureTokenService secureTokenService;

    public AuthTokenService(AuthTokenRepository authTokenRepository, SecureTokenService secureTokenService) {
        this.authTokenRepository = authTokenRepository;
        this.secureTokenService = secureTokenService;
    }

    @Transactional
    public String createToken(User user) {
        revokeActiveTokensForUser(user);

        String rawToken = secureTokenService.generateRawToken();

        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setTokenHash(secureTokenService.hashToken(rawToken));
        authToken.setExpiresAt(LocalDateTime.now().plusMinutes(AUTH_TOKEN_MINUTES));
        authToken.setRevoked(false);
        authTokenRepository.save(authToken);

        return rawToken;
    }

    private void revokeActiveTokensForUser(User user) {
        for (AuthToken token : authTokenRepository.findByUserIdAndRevokedFalse(user.getId())) {
            token.setRevoked(true);
            authTokenRepository.save(token);
        }
    }

    public Optional<User> findUserByRawToken(String rawToken) {
        String tokenHash = secureTokenService.hashToken(rawToken);
        return authTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(AuthToken::getUser);
    }

    @Transactional
    public void revokeRawToken(String rawToken) {
        String tokenHash = secureTokenService.hashToken(rawToken);
        authTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            authTokenRepository.save(token);
        });
    }
}
