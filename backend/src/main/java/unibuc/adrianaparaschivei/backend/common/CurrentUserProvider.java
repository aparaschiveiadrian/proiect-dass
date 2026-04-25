package unibuc.adrianaparaschivei.backend.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.service.UserService;

import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUserProvider {
    private final UserService userService;

    public CurrentUserProvider(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> from(HttpServletRequest request) {
        Optional<String> cookieValue = CookieReader.findCookieValue(request, AuthCookieNames.AUTH_COOKIE);
        if (cookieValue.isEmpty()) {
            return Optional.empty();
        }

        UUID userId = UUID.fromString(cookieValue.get());
        return userService.findById(userId);
    }
}
