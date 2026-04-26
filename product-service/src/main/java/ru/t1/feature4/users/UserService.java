package ru.t1.feature4.users;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
    }

    public User create(String username) {
        validateUsername(username);
        return userDao.create(username);
    }

    public Optional<User> getOne(Long id) {
        validateId(id);
        return userDao.findById(id);
    }

    public List<User> getAll() {
        return userDao.findAll();
    }

    public Optional<User> updateUsername(Long id, String username) {
        validateId(id);
        validateUsername(username);
        return userDao.update(new User(id, username));
    }

    public boolean delete(Long id) {
        validateId(id);
        return userDao.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
    }
}
