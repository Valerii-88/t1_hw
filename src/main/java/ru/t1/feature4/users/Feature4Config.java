package ru.t1.feature4.users;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Feature4Config {
    private static final String DEFAULT_DB_URL =
            "jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:6543/postgres?sslmode=require";
    private static final String DEFAULT_DB_USERNAME = "feature4_app.qdcvoukgdmgntpmathjr";

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(readSetting("supabase.db.url", "SUPABASE_DB_URL", DEFAULT_DB_URL));
        config.setUsername(readSetting("supabase.db.username", "SUPABASE_DB_USERNAME", DEFAULT_DB_USERNAME));
        config.setPassword(readRequiredSetting("supabase.db.password", "SUPABASE_DB_PASSWORD"));
        config.setMaximumPoolSize(5);
        config.setPoolName("feature4-users-pool");
        config.addDataSourceProperty("prepareThreshold", "0");
        return new HikariDataSource(config);
    }

    @Bean
    public UserDao userDao(HikariDataSource dataSource) {
        return new UserDao(dataSource);
    }

    @Bean
    public UserService userService(UserDao userDao) {
        return new UserService(userDao);
    }

    private String readSetting(String propertyName, String environmentName, String defaultValue) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String readRequiredSetting(String propertyName, String environmentName) {
        String value = readSetting(propertyName, environmentName, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing database setting. Set JVM property '" + propertyName
                            + "' or environment variable '" + environmentName + "'."
            );
        }
        return value;
    }
}
