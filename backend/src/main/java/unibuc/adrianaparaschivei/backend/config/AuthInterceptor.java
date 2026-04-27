package unibuc.adrianaparaschivei.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import unibuc.adrianaparaschivei.backend.common.AuthCookieNames;
import unibuc.adrianaparaschivei.backend.common.CookieReader;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestedPath = request.getRequestURI();

        if (isPublicPage(requestedPath)) {
            return true;
        }

        boolean hasAuthCookie = CookieReader.findCookieValue(request, AuthCookieNames.AUTH_COOKIE).isPresent();
        if (!hasAuthCookie) {
            response.sendRedirect("/login?error=Please login first");
            return false;
        }

        return true;
    }

    private boolean isPublicPage(String requestedPath) {
        if (isPublicApiPage(requestedPath)) {
            return true;
        }

        return !requestedPath.startsWith("/dashboard")
                && !requestedPath.startsWith("/tickets")
                && !requestedPath.startsWith("/audit")
                && !requestedPath.startsWith("/api")
                && !requestedPath.startsWith("/logout");
    }

    private boolean isPublicApiPage(String requestedPath) {
        return requestedPath.equals("/api/register")
                || requestedPath.equals("/api/login")
                || requestedPath.equals("/api/forgot-password")
                || requestedPath.equals("/api/reset-password");
    }
}
