package unibuc.adrianaparaschivei.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.dto.UserRegisterRequestDto;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.UserRepository;
import unibuc.adrianaparaschivei.backend.repository.VulnerableUserSqlRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VulnerableUserSqlRepository vulnerableUserSqlRepository;

    public UserService(UserRepository userRepository, VulnerableUserSqlRepository vulnerableUserSqlRepository) {
        this.userRepository = userRepository;
        this.vulnerableUserSqlRepository = vulnerableUserSqlRepository;
    }

    @Transactional
    public User registerVulnerable(UserRegisterRequestDto request) {
        User user = mapToVulnerableUser(request);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    public Optional<User> findByEmailUnsafe(String email) {
        return vulnerableUserSqlRepository.findByEmailUnsafe(normalizeEmail(email));
    }

    public Optional<User> loginUnsafe(String email, String password) {
        String passwordHash = VulnerablePasswordHasher.md5(password == null ? "" : password);
        return vulnerableUserSqlRepository.loginUnsafe(normalizeEmail(email), passwordHash);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    @Transactional
    public void updatePasswordVulnerable(User user, String newPassword) {
        user.setPasswordHash(VulnerablePasswordHasher.md5(newPassword));
        userRepository.save(user);
    }

    private User mapToVulnerableUser(UserRegisterRequestDto request) {
        User user = new User();
        user.setEmail(normalizeEmail(request.email()));
        user.setPasswordHash(VulnerablePasswordHasher.md5(request.password()));
        user.setRole(Role.ANALYST);
        user.setLocked(false);
        return user;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
