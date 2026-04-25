package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibuc.adrianaparaschivei.backend.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
