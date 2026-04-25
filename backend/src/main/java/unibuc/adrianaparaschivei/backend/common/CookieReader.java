package unibuc.adrianaparaschivei.backend.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public final class CookieReader {
    private CookieReader() {
    }

    public static Optional<String> findCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return Optional.of(cookie.getValue());
            }
        }

        return Optional.empty();
    }
}
