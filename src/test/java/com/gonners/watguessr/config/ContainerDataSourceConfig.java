package com.gonners.watguessr.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerDataSourceConfig {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    )
            .withDatabaseName("watguessr")
            .withUsername("watuser")
            .withPassword("goon");

    static {
        POSTGRES.start();
    }

    @Bean
    public DataSource dataSource() {
        Properties properties = new Properties();
        properties.setProperty("ApplicationName", "WatGuessrTests");
        properties.setProperty("currentSchema", "watguessr");

        String jdbcUrl = POSTGRES.getJdbcUrl();
        if (!jdbcUrl.contains("currentSchema=")) {
            jdbcUrl = jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "currentSchema=watguessr";
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(POSTGRES.getUsername());
        hikariConfig.setPassword(POSTGRES.getPassword());
        hikariConfig.setDriverClassName(POSTGRES.getDriverClassName());
        hikariConfig.setDataSourceProperties(properties);
        return new HikariDataSource(hikariConfig);
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return POSTGRES;
    }
} 