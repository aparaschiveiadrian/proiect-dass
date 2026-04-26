package unibuc.adrianaparaschivei.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unibuc.adrianaparaschivei.backend.dto.UserRegisterRequestDto;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.User;
import unibuc.adrianaparaschivei.backend.repository.UserLookupRepository;
import unibuc.adrianaparaschivei.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;
    private static final String DUMMY_BCRYPT_HASH = new BCryptPasswordEncoder().encode("ParolaTemporara123!");

    private final UserRepository userRepository;
    private final UserLookupRepository userLookupRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserLookupRepository userLookupRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userLookupRepository = userLookupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(UserRegisterRequestDto request) {
        User user = mapToUser(request);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userLookupRepository.findByEmail(normalizeEmail(email));
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userLookupRepository.existsByEmail(normalizeEmail(email));
    }

    public boolean passwordMatches(String rawPassword, User user) {
        return passwordEncoder.matches(rawPassword == null ? "" : rawPassword, user.getPasswordHash());
    }

    public void performDummyPasswordCheck(String rawPassword) {
        passwordEncoder.matches(rawPassword == null ? "" : rawPassword, DUMMY_BCRYPT_HASH);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public boolean recordFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        boolean accountWasLocked = false;

        if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            accountWasLocked = true;
        }

        userRepository.save(user);
        return accountWasLocked;
    }

    @Transactional
    public void resetFailedLogins(User user) {
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    @Transactional
    public boolean isAccountLocked(User user) {
        if (!user.isLocked()) {
            return false;
        }

        LocalDateTime lockedUntil = user.getLockedUntil();
        if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
            return true;
        }

        resetFailedLogins(user);
        return false;
    }

    private User mapToUser(UserRegisterRequestDto request) {
        User user = new User();
        user.setEmail(normalizeEmail(request.email()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ANALYST);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return user;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
