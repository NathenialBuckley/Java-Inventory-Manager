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

/**
 * Database configuration for multi-platform deployment.
 *
 * This configuration handles database connectivity for different hosting platforms:
 * - Render: Uses DATABASE_URL environment variable (format: postgres://user:pass@host:port/db)
 * - Railway: Uses individual environment variables (PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE)
 * - Local Development: Falls back to localhost with default PostgreSQL port
 *
 * The configuration automatically:
 * 1. Detects which platform is being used based on environment variables
 * 2. Parses the database URL correctly for each platform
 * 3. Adds SSL mode for production databases (Render)
 * 4. Provides extensive logging for debugging connection issues
 *
 * DATABASE_URL Parsing:
 * - Render format: postgres://username:password@host/database (port optional, defaults to 5432)
 * - Converted to JDBC: jdbc:postgresql://host:5432/database?sslmode=require
 *
 * This eliminates the need for manual database configuration when deploying.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    /**
     * JDBC URL from application.properties, used as fallback for Railway/local development.
     * Format: jdbc:postgresql://host:port/database
     */
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    /**
     * Database username from application.properties, used as fallback.
     */
    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    /**
     * Database password from application.properties, used as fallback.
     */
    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    /**
     * JDBC driver class name, defaults to PostgreSQL driver.
     */
    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    /**
     * Creates and configures the DataSource bean.
     *
     * This method is the heart of the database configuration. It:
     * 1. Checks for DATABASE_URL environment variable (Render deployment)
     * 2. If found, parses it and creates DataSource with SSL
     * 3. If not found, uses application.properties values (Railway/local)
     *
     * The method includes extensive logging to help diagnose connection issues
     * during deployment.
     *
     * @return Configured DataSource ready for use by Spring Data JPA
     * @throws RuntimeException if DATABASE_URL is malformed or connection fails
     */
    @Bean
    public DataSource dataSource() {
        // Check for Render's DATABASE_URL environment variable
        String databaseUrl = System.getenv("DATABASE_URL");

        logger.info("=== DATABASE CONFIGURATION DEBUG ===");
        logger.info("DATABASE_URL environment variable: {}", databaseUrl != null ? "SET (length: " + databaseUrl.length() + ")" : "NOT SET");

        // BRANCH 1: Render deployment (DATABASE_URL exists)
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            logger.info("DATABASE_URL found, parsing for Render deployment");
            logger.info("DATABASE_URL starts with: {}", databaseUrl.substring(0, Math.min(20, databaseUrl.length())) + "...");

            // Render provides DATABASE_URL in format: postgres://user:password@host:port/database
            // We need to convert it to JDBC format: jdbc:postgresql://host:port/database
            try {
                // Parse the URL using Java's URI class
                URI dbUri = new URI(databaseUrl);

                // Log parsed components for debugging
                logger.info("URI scheme: {}", dbUri.getScheme());
                logger.info("URI host: {}", dbUri.getHost());
                logger.info("URI port: {}", dbUri.getPort());
                logger.info("URI path: {}", dbUri.getPath());
                logger.info("URI userInfo present: {}", dbUri.getUserInfo() != null);

                // Validate that credentials are present in the URL
                // UserInfo format should be: "username:password"
                if (dbUri.getUserInfo() == null || !dbUri.getUserInfo().contains(":")) {
                    logger.error("DATABASE_URL does not contain user credentials in expected format");
                    throw new RuntimeException("DATABASE_URL missing credentials");
                }

                // Extract username and password from userInfo
                // Using split with limit=2 to handle passwords containing ':'
                String[] credentials = dbUri.getUserInfo().split(":", 2);
                String username = credentials[0];
                String password = credentials.length > 1 ? credentials[1] : "";

                // Get port number, default to PostgreSQL standard port if not specified
                // Render's DATABASE_URL may omit the port, so we default to 5432
                int port = dbUri.getPort();
                if (port == -1) {
                    port = 5432; // Default PostgreSQL port
                    logger.info("Port not specified in URL, defaulting to 5432");
                }

                // Build the JDBC URL with SSL requirement
                // Render's managed PostgreSQL requires SSL connections
                // Format: jdbc:postgresql://host:port/database?sslmode=require
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + port + dbUri.getPath() + "?sslmode=require";

                logger.info("Final JDBC URL: {}", jdbcUrl);
                logger.info("Using username: {}", username);
                logger.info("Password present: {}", !password.isEmpty());

                // Build and return the DataSource
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

        // BRANCH 2: Railway/Local deployment (use application.properties)
        logger.info("DATABASE_URL not found, using application.properties configuration");
        logger.info("Fallback datasource URL: {}", datasourceUrl);
        logger.info("Fallback username: {}", datasourceUsername);
        logger.info("Fallback password present: {}", datasourcePassword != null && !datasourcePassword.isEmpty());

        // Build DataSource from application.properties values
        // These come from @Value injected fields at the top of this class
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
