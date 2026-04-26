package ru.t1.feature6;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Feature6Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Feature6Application.class)
                .web(WebApplicationType.SERVLET)
                .logStartupInfo(false)
                .run(args);
    }
}
