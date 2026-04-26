package ru.t1.feature4.users;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:feature4.properties")
public class Feature4Config {
    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(Environment environment) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getRequiredProperty(environment, "feature4.datasource.url"));
        config.setUsername(getRequiredProperty(environment, "feature4.datasource.username"));
        config.setPassword(getRequiredProperty(environment, "feature4.datasource.password"));
        config.setMaximumPoolSize(Integer.parseInt(getRequiredProperty(environment, "feature4.datasource.maximum-pool-size")));
        config.setPoolName(getRequiredProperty(environment, "feature4.datasource.pool-name"));
        config.addDataSourceProperty("prepareThreshold", getRequiredProperty(environment, "feature4.datasource.prepare-threshold"));
        return new HikariDataSource(config);
    }

    private String getRequiredProperty(Environment environment, String propertyName) {
        String value = environment.resolvePlaceholders(environment.getProperty(propertyName, ""));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required setting '" + propertyName + "'");
        }
        return value;
    }
}
