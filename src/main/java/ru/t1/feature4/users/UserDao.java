package ru.t1.feature4.users;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserDao {
    private static final String TABLE_NAME = "public.users";

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    public User create(String username) {
        String sql = "insert into " + TABLE_NAME + " (username) values (?) returning id, username";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to create user", exception);
        }

        throw new IllegalStateException("Database did not return created user");
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, username from " + TABLE_NAME + " where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find user by id", exception);
        }
    }

    public List<User> findAll() {
        String sql = "select id, username from " + TABLE_NAME + " order by id";
        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to find all users", exception);
        }
    }

    public Optional<User> update(User user) {
        String sql = "update " + TABLE_NAME + " set username = ? where id = ? returning id, username";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setLong(2, user.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update user", exception);
        }
    }

    public boolean deleteById(Long id) {
        String sql = "delete from " + TABLE_NAME + " where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete user", exception);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(resultSet.getLong("id"), resultSet.getString("username"));
    }
}
