package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VulnerableUserSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public VulnerableUserSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByEmailUnsafe(String email) {
        String sql = """
                select id, email, password_hash, role, created_at, locked
                from users
                where email = '%s'
                """.formatted(email);

        return jdbcTemplate.query(sql, this::mapUser).stream().findFirst();
    }

    public Optional<User> loginUnsafe(String email, String passwordHash) {
        String sql = """
                select id, email, password_hash, role, created_at, locked
                from users
                where email = '%s'
                  and password_hash = '%s'
                """.formatted(email, passwordHash);

        return jdbcTemplate.query(sql, this::mapUser).stream().findFirst();
    }

    private User mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(resultSet.getObject("id", UUID.class));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        user.setLocked(resultSet.getBoolean("locked"));
        return user;
    }
}
