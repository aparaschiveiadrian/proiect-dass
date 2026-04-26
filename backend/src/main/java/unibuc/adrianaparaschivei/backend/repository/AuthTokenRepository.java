package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibuc.adrianaparaschivei.backend.model.AuthToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    Optional<AuthToken> findByTokenHash(String tokenHash);

    List<AuthToken> findByUserIdAndRevokedFalse(UUID userId);
}
