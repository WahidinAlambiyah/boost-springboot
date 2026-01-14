package com.example.graphqlusers.repository;

import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PostgresUserRepository implements UserRepository {
    private final DataSource dataSource;

    public PostgresUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT id, username, email, full_name, password_hash, created_at, updated_at FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                User user = mapUser(rs, loadRoles(connection, id));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user", e);
        }
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT id, username, email, full_name, password_hash, created_at, updated_at FROM users WHERE username = ? OR email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usernameOrEmail);
            statement.setString(2, usernameOrEmail);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                UUID id = rs.getObject("id", UUID.class);
                User user = mapUser(rs, loadRoles(connection, id));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, full_name, password_hash, created_at, updated_at FROM users WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                UUID id = rs.getObject("id", UUID.class);
                User user = mapUser(rs, loadRoles(connection, id));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, full_name, password_hash, created_at, updated_at FROM users WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                UUID id = rs.getObject("id", UUID.class);
                User user = mapUser(rs, loadRoles(connection, id));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user", e);
        }
    }

    @Override
    public List<User> list(int page, int size) {
        String sql = "SELECT id, username, email, full_name, password_hash, created_at, updated_at FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, size);
            statement.setInt(2, page * size);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    UUID id = rs.getObject("id", UUID.class);
                    users.add(mapUser(rs, loadRoles(connection, id)));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list users", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count users", e);
        }
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (id, username, email, full_name, password_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, user.id());
            statement.setString(2, user.username());
            statement.setString(3, user.email());
            statement.setString(4, user.fullName());
            statement.setString(5, user.passwordHash());
            statement.setTimestamp(6, Timestamp.from(user.createdAt()));
            statement.setTimestamp(7, Timestamp.from(user.updatedAt()));
            statement.executeUpdate();
            saveRoles(connection, user.id(), user.roles());
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public User update(UUID id, String email, String fullName, String passwordHash, Set<Role> roles) {
        String sql = "UPDATE users SET email = COALESCE(?, email), full_name = COALESCE(?, full_name), password_hash = COALESCE(?, password_hash), updated_at = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, fullName);
            statement.setString(3, passwordHash);
            statement.setTimestamp(4, Timestamp.from(Instant.now()));
            statement.setObject(5, id);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                return null;
            }
            if (roles != null) {
                replaceRoles(connection, id, roles);
            }
            return findById(id).orElse(null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private Set<Role> loadRoles(Connection connection, UUID userId) throws SQLException {
        String sql = "SELECT role FROM user_roles WHERE user_id = ?";
        Set<Role> roles = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    roles.add(Role.valueOf(rs.getString("role")));
                }
            }
        }
        return roles;
    }

    private void saveRoles(Connection connection, UUID userId, Set<Role> roles) throws SQLException {
        String sql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Role role : roles) {
                statement.setObject(1, userId);
                statement.setString(2, role.name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceRoles(Connection connection, UUID userId, Set<Role> roles) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM user_roles WHERE user_id = ?")) {
            delete.setObject(1, userId);
            delete.executeUpdate();
        }
        saveRoles(connection, userId, roles);
    }

    private User mapUser(ResultSet rs, Set<Role> roles) throws SQLException {
        return new User(
                rs.getObject("id", UUID.class),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("full_name"),
                rs.getString("password_hash"),
                roles,
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
