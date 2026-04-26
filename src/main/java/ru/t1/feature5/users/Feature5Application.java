package ru.t1.feature5.users;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Feature5Application {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext ignored =
                     new SpringApplicationBuilder(Feature5Application.class)
                             .logStartupInfo(false)
                             .properties("spring.main.web-application-type=none")
                             .run(args)) {
        }
    }
}
