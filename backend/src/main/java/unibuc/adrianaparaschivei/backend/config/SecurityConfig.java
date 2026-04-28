package unibuc.adrianaparaschivei.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .exceptionHandling(exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api")) {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"Forbidden\"}");
                        return;
                    }
                    response.sendError(403);
                }))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .build();
    }
}
