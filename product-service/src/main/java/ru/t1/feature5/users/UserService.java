package ru.t1.feature5.users;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(String username) {
        validateUsername(username);
        return userRepository.save(new User(null, username));
    }

    public Optional<User> getOne(Long id) {
        validateId(id);
        return userRepository.findById(id);
    }

    public List<User> getAll() {
        return userRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public Optional<User> updateUsername(Long id, String username) {
        validateId(id);
        validateUsername(username);

        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(username);
                    return userRepository.save(user);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        validateId(id);
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
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
