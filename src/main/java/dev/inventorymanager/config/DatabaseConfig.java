package dev.inventorymanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        logger.info("=== DATABASE CONFIGURATION DEBUG ===");
        logger.info("DATABASE_URL environment variable: {}", databaseUrl != null ? "SET (length: " + databaseUrl.length() + ")" : "NOT SET");

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            logger.info("DATABASE_URL found, parsing for Render deployment");
            logger.info("DATABASE_URL starts with: {}", databaseUrl.substring(0, Math.min(20, databaseUrl.length())) + "...");

            // Render provides DATABASE_URL in format: postgres://user:password@host:port/database
            // We need to convert it to JDBC format: jdbc:postgresql://host:port/database
            try {
                URI dbUri = new URI(databaseUrl);

                logger.info("URI scheme: {}", dbUri.getScheme());
                logger.info("URI host: {}", dbUri.getHost());
                logger.info("URI port: {}", dbUri.getPort());
                logger.info("URI path: {}", dbUri.getPath());
                logger.info("URI userInfo present: {}", dbUri.getUserInfo() != null);

                if (dbUri.getUserInfo() == null || !dbUri.getUserInfo().contains(":")) {
                    logger.error("DATABASE_URL does not contain user credentials in expected format");
                    throw new RuntimeException("DATABASE_URL missing credentials");
                }

                String[] credentials = dbUri.getUserInfo().split(":", 2);
                String username = credentials[0];
                String password = credentials.length > 1 ? credentials[1] : "";

                // Get port, default to 5432 if not specified
                int port = dbUri.getPort();
                if (port == -1) {
                    port = 5432; // Default PostgreSQL port
                }

                // Render requires SSL connections - add sslmode parameter
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + port + dbUri.getPath() + "?sslmode=require";

                logger.info("Final JDBC URL: {}", jdbcUrl);
                logger.info("Using username: {}", username);
                logger.info("Password present: {}", !password.isEmpty());

                DataSource ds = DataSourceBuilder
                        .create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();

                logger.info("DataSource created successfully from DATABASE_URL");
                return ds;
            } catch (URISyntaxException e) {
                logger.error("Failed to parse DATABASE_URL", e);
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            } catch (Exception e) {
                logger.error("Error creating DataSource from DATABASE_URL", e);
                throw new RuntimeException("Failed to create DataSource", e);
            }
        }

        logger.info("DATABASE_URL not found, using application.properties configuration");
        logger.info("Fallback datasource URL: {}", datasourceUrl);
        logger.info("Fallback username: {}", datasourceUsername);
        logger.info("Fallback password present: {}", datasourcePassword != null && !datasourcePassword.isEmpty());

        // Fallback to application.properties configuration (for Railway, local dev, etc.)
        DataSource ds = DataSourceBuilder
                .create()
                .url(datasourceUrl)
                .username(datasourceUsername)
                .password(datasourcePassword)
                .driverClassName(driverClassName)
                .build();

        logger.info("DataSource created successfully from application.properties");
        return ds;
    }
}
