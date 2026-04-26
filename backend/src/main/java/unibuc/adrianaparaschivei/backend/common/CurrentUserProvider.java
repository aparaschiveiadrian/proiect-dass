package unibuc.adrianaparaschivei.backend.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.service.AuthTokenService;

import java.util.Optional;

@Component
public class CurrentUserProvider {
    private final AuthTokenService authTokenService;

    public CurrentUserProvider(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    public Optional<User> from(HttpServletRequest request) {
        Optional<String> cookieValue = CookieReader.findCookieValue(request, AuthCookieNames.AUTH_COOKIE);
        if (cookieValue.isEmpty()) {
            return Optional.empty();
        }

        String rawToken = cookieValue.get();
        return authTokenService.findUserByRawToken(rawToken);
    }
}
