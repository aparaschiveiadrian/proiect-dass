package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserLookupRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserLookupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                select id,
                       email,
                       password_hash,
                       role,
                       created_at,
                       locked,
                       failed_login_attempts,
                       locked_until
                from users
                where email = ?
                """;

        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            return statement;
        }, this::mapUser).stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        String sql = """
                select count(*)
                from users
                where email = ?
                """;

        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            return statement;
        }, resultSet -> {
            if (!resultSet.next()) {
                return false;
            }
            return resultSet.getInt(1) > 0;
        });
    }

    private User mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(resultSet.getObject("id", UUID.class));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        user.setLocked(resultSet.getBoolean("locked"));
        user.setFailedLoginAttempts(resultSet.getInt("failed_login_attempts"));

        if (resultSet.getTimestamp("locked_until") != null) {
            user.setLockedUntil(resultSet.getTimestamp("locked_until").toLocalDateTime());
        }

        return user;
    }
}
