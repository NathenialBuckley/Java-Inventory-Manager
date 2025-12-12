package dev.inventorymanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            logger.info("DATABASE_URL found, parsing for Render deployment");
            // Render provides DATABASE_URL in format: postgres://user:password@host:port/database
            // We need to convert it to JDBC format: jdbc:postgresql://host:port/database
            try {
                URI dbUri = new URI(databaseUrl);

                if (dbUri.getUserInfo() == null || !dbUri.getUserInfo().contains(":")) {
                    logger.error("DATABASE_URL does not contain user credentials in expected format");
                    throw new RuntimeException("DATABASE_URL missing credentials");
                }

                String[] credentials = dbUri.getUserInfo().split(":");
                String username = credentials[0];
                String password = credentials.length > 1 ? credentials[1] : "";

                // Render requires SSL connections - add sslmode parameter
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

                logger.info("Connecting to database at: {}:{}{}", dbUri.getHost(), dbUri.getPort(), dbUri.getPath());
                logger.info("Using username: {}", username);

                return DataSourceBuilder
                        .create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } catch (URISyntaxException e) {
                logger.error("Failed to parse DATABASE_URL: {}", databaseUrl, e);
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            }
        }

        logger.info("DATABASE_URL not found, using application.properties configuration");
        // Fallback to application.properties configuration (for Railway, local dev, etc.)
        return DataSourceBuilder
                .create()
                .build();
    }
}
