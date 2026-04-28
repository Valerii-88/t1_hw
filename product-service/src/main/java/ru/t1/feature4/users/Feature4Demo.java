package ru.t1.feature4.users;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public final class Feature4Demo {
    private Feature4Demo() {
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan("ru.t1.feature4.users");
            context.refresh();
            UserService userService = context.getBean(UserService.class);
            String username = "feature4_user_" + System.currentTimeMillis();

            User createdUser = userService.create(username);
            System.out.println("Создан пользователь: " + createdUser);

            User foundUser = userService.getOne(createdUser.getId()).orElseThrow();
            System.out.println("Получен один пользователь: " + foundUser);

            List<User> users = userService.getAll();
            System.out.println("Получены все пользователи, количество: " + users.size());
            users.forEach(user -> System.out.println("  " + user));

            User updatedUser = userService.updateUsername(createdUser.getId(), username + "_updated").orElseThrow();
            System.out.println("Обновлен пользователь: " + updatedUser);

            boolean deleted = userService.delete(updatedUser.getId());
            System.out.println("Удален пользователь: " + deleted);

            boolean existsAfterDelete = userService.getOne(updatedUser.getId()).isPresent();
            System.out.println("Пользователь найден после удаления: " + existsAfterDelete);
        }
    }
}
