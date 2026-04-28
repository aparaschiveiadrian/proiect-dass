package unibuc.adrianaparaschivei.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import unibuc.adrianaparaschivei.backend.common.CurrentUserProvider;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final CurrentUserProvider currentUserProvider;

    public AuthInterceptor(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestedPath = request.getRequestURI();

        if (isPublicPage(requestedPath)) {
            return true;
        }

        boolean hasValidAuthToken = currentUserProvider.from(request).isPresent();
        if (!hasValidAuthToken) {
            if (requestedPath.startsWith("/api")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Please login first\"}");
                return false;
            }
            response.sendRedirect("/login?error=Please login first");
            return false;
        }

        return true;
    }

    private boolean isPublicPage(String requestedPath) {
        return requestedPath.equals("/")
                || requestedPath.startsWith("/register")
                || requestedPath.startsWith("/login")
                || requestedPath.startsWith("/forgot-password")
                || requestedPath.startsWith("/reset-password")
                || requestedPath.equals("/api/register")
                || requestedPath.equals("/api/login")
                || requestedPath.equals("/api/forgot-password")
                || requestedPath.equals("/api/reset-password")
                || requestedPath.equals("/api/csrf");
    }
}
