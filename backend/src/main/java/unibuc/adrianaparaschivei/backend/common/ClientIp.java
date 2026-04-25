package unibuc.adrianaparaschivei.backend.common;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIp {
    private ClientIp() {
    }

    public static String from(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}