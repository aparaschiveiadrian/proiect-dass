package unibuc.adrianaparaschivei.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import unibuc.adrianaparaschivei.backend.model.Role;
import unibuc.adrianaparaschivei.backend.model.Ticket;
import unibuc.adrianaparaschivei.backend.model.TicketSeverity;
import unibuc.adrianaparaschivei.backend.model.TicketStatus;
import unibuc.adrianaparaschivei.backend.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class TicketSearchRepository {
    private final JdbcTemplate jdbcTemplate;

    public TicketSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Ticket> searchOwnTickets(UUID ownerId, String query, int limit) {
        String sql = """
                select t.id,
                       t.title,
                       t.description,
                       t.severity,
                       t.status,
                       t.created_at,
                       t.updated_at,
                       u.id as owner_id,
                       u.email as owner_email,
                       u.role as owner_role,
                       u.created_at as owner_created_at,
                       u.locked as owner_locked
                from tickets t
                join users u on u.id = t.owner_id
                where t.owner_id = ?
                  and (
                        lower(t.title) like lower(?)
                        or lower(t.description) like lower(?)
                  )
                order by t.created_at desc
                limit ?
                """;

        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            String searchPattern = "%" + query + "%";
            statement.setObject(1, ownerId);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setInt(4, limit);
            return statement;
        }, this::mapTicket);
    }

    public List<Ticket> searchAllTickets(String query, int limit) {
        String sql = """
                select t.id,
                       t.title,
                       t.description,
                       t.severity,
                       t.status,
                       t.created_at,
                       t.updated_at,
                       u.id as owner_id,
                       u.email as owner_email,
                       u.role as owner_role,
                       u.created_at as owner_created_at,
                       u.locked as owner_locked
                from tickets t
                join users u on u.id = t.owner_id
                where lower(t.title) like lower(?)
                   or lower(t.description) like lower(?)
                order by t.created_at desc
                limit ?
                """;

        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            String searchPattern = "%" + query + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setInt(3, limit);
            return statement;
        }, this::mapTicket);
    }

    private Ticket mapTicket(ResultSet resultSet, int rowNumber) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(resultSet.getObject("id", UUID.class));
        ticket.setTitle(resultSet.getString("title"));
        ticket.setDescription(resultSet.getString("description"));
        ticket.setSeverity(TicketSeverity.valueOf(resultSet.getString("severity")));
        ticket.setStatus(TicketStatus.valueOf(resultSet.getString("status")));
        ticket.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        ticket.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        ticket.setOwner(mapOwner(resultSet));
        return ticket;
    }

    private User mapOwner(ResultSet resultSet) throws SQLException {
        User owner = new User();
        owner.setId(resultSet.getObject("owner_id", UUID.class));
        owner.setEmail(resultSet.getString("owner_email"));
        owner.setRole(Role.valueOf(resultSet.getString("owner_role")));
        owner.setCreatedAt(resultSet.getTimestamp("owner_created_at").toLocalDateTime());
        owner.setLocked(resultSet.getBoolean("owner_locked"));
        return owner;
    }
}
