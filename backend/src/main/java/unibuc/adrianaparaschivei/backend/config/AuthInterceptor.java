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
            response.sendRedirect("/login?error=Please login first");
            return false;
        }

        return true;
    }

    private boolean isPublicPage(String requestedPath) {
        return !requestedPath.startsWith("/dashboard")
                && !requestedPath.startsWith("/tickets")
                && !requestedPath.startsWith("/audit")
                && !requestedPath.startsWith("/logout");
    }
}
