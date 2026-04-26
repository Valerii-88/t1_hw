package ru.t1.feature5.users;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Feature5CommandLineRunner implements CommandLineRunner {
    private final UserService userService;

    public Feature5CommandLineRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        System.out.println("Начальный набор пользователей:");
        printUsers(userService.getAll());

        String username = "feature5_user_" + System.currentTimeMillis();
        User createdUser = userService.create(username);
        System.out.println("Создан пользователь: " + createdUser);

        User foundUser = userService.getOne(createdUser.getId()).orElseThrow();
        System.out.println("Получен один пользователь: " + foundUser);

        User updatedUser = userService.updateUsername(createdUser.getId(), username + "_updated").orElseThrow();
        System.out.println("Обновлен пользователь: " + updatedUser);

        System.out.println("Пользователи после изменений:");
        printUsers(userService.getAll());

        boolean deleted = userService.delete(updatedUser.getId());
        System.out.println("Удален пользователь: " + deleted);

        boolean existsAfterDelete = userService.getOne(updatedUser.getId()).isPresent();
        System.out.println("Пользователь найден после удаления: " + existsAfterDelete);
    }

    private void printUsers(List<User> users) {
        users.forEach(user -> System.out.println("  " + user));
    }
}
