# Deployment Guide

## Quick Reference

### Development Mode (Local)
```bash
# Uses H2 in-memory database
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Or directly
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access
# - App: http://localhost:8080
# - H2 Console: http://localhost:8080/h2-console
```

### Production Mode (Local PostgreSQL)
```bash
# Set profile and database connection
export SPRING_PROFILES_ACTIVE=prod
export PGHOST=localhost
export PGPORT=5432
export PGDATABASE=inventorydb
export PGUSER=your_username
export PGPASSWORD=your_password

mvn spring-boot:run
```

## Deployment Platforms

### Render Deployment

#### Setup Steps:
1. Push your code to GitHub
2. Create a **PostgreSQL database** in Render dashboard
3. Create a **Web Service** in Render
4. Connect to your GitHub repository
5. Configure the service:

**Environment Variables:**
```
SPRING_PROFILES_ACTIVE=prod
```

**Build Command:**
```bash
mvn clean package -DskipTests
```

**Start Command:**
```bash
java -jar target/inventory-manager-1.0-SNAPSHOT.jar
```

**Notes:**
- Render automatically provides `DATABASE_URL` environment variable
- DatabaseConfig.java automatically detects and parses it
- SSL is automatically configured
- No additional database configuration needed

#### Troubleshooting Render:
- Check logs: Click on "Logs" in Render dashboard
- Verify DATABASE_URL is set: Check "Environment" tab
- Database connection: Ensure PostgreSQL instance is running
- Build failures: Check Maven output in logs

---

### Railway Deployment

#### Setup Steps:
1. Push your code to GitHub
2. Create new project in Railway from GitHub
3. Add PostgreSQL database to project
4. Railway automatically provides database environment variables

**Environment Variables to Set:**
```
SPRING_PROFILES_ACTIVE=prod
```

**Notes:**
- Railway automatically provides: `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
- DatabaseConfig.java uses these variables automatically
- No DATABASE_URL needed (Railway uses individual vars)
- SSL is automatically configured

#### Troubleshooting Railway:
- Check logs: Click on deployment logs
- Database connection: Verify PostgreSQL service is healthy
- Environment variables: Ensure SPRING_PROFILES_ACTIVE=prod is set

---

## Environment Variables Reference

### Required for Production

| Variable | Description | Example | Source |
|----------|-------------|---------|--------|
| `SPRING_PROFILES_ACTIVE` | Activates production profile | `prod` | Manual |
| `DATABASE_URL` | Full database URL (Render) | `postgres://user:pass@host/db` | Render |
| `PGHOST` | Database host (Railway) | `postgres.railway.internal` | Railway |
| `PGPORT` | Database port (Railway) | `5432` | Railway |
| `PGDATABASE` | Database name (Railway) | `railway` | Railway |
| `PGUSER` | Database username (Railway) | `postgres` | Railway |
| `PGPASSWORD` | Database password (Railway) | `secret123` | Railway |

### Optional

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `PORT` | Server port | `8080` | `8080` |
| `DB_SCHEMA` | Database schema | `public` | `public` |

---

## How DatabaseConfig Works

The `DatabaseConfig.java` file automatically detects your deployment platform:

```java
@Profile("prod")  // Only active in production
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // PRIORITY 1: Check for DATABASE_URL (Render)
        if (DATABASE_URL exists) {
            Parse URL -> Extract credentials -> Add SSL
            Return configured DataSource
        }

        // PRIORITY 2: Use PGHOST, PGPORT, etc (Railway)
        else {
            Use application-prod.properties values
            Return configured DataSource
        }
    }
}
```

### Render Detection:
- Looks for `DATABASE_URL` environment variable
- Format: `postgres://username:password@host:port/database`
- Converts to: `jdbc:postgresql://host:port/database?sslmode=require`
- Automatically extracts username and password

### Railway Detection:
- Uses standard PostgreSQL environment variables
- Reads from `application-prod.properties`
- Connects using: `jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}`
- SSL configured via connection string

---

## Database Schema Management

### Development (H2):
- Schema automatically created on startup
- `ddl-auto=update` - tables created from entities
- Database destroyed on shutdown (in-memory)
- Perfect for testing without persistence

### Production (PostgreSQL):
- Schema automatically updated by Hibernate
- `ddl-auto=update` - safe for adding columns
- **Important**: Hibernate will NOT drop tables or columns
- Manual migrations needed for dropping/renaming

### Schema Updates:
When you add new fields to entities (like we did with Transaction):
1. Hibernate detects new fields
2. Generates ALTER TABLE statements
3. Adds columns automatically
4. Existing data is preserved

**Limitations:**
- Cannot rename columns (requires manual migration)
- Cannot drop columns (requires manual migration)
- NOT NULL constraints on existing tables may fail

---

## Monitoring and Logs

### Development:
```bash
# Verbose SQL logging
tail -f logs/spring.log
```

### Production (Render):
```bash
# Via Render dashboard
# Logs -> Click on your service
# Or use Render CLI
render logs -s your-service-name
```

### Production (Railway):
```bash
# Via Railway dashboard
# Click on deployment -> View logs
# Or use Railway CLI
railway logs
```

---

## Health Checks

### Application Health:
```bash
# Check if app is running
curl http://localhost:8080/

# Expected: HTML page (login screen)
```

### Database Health:
```bash
# Dev mode - Access H2 console
# http://localhost:8080/h2-console

# Prod mode - Check connection
# Look for "HikariPool-1 - Start completed" in logs
```

---

## Common Issues

### Issue: "column does not exist"
**Cause**: Database schema not updated
**Solution**:
- Restart application (Hibernate will run schema updates)
- Check logs for ALTER TABLE statements
- For complex changes, may need manual migration

### Issue: "Connection refused"
**Cause**: Database not accessible
**Solution**:
- Verify database service is running
- Check environment variables are set correctly
- Ensure DATABASE_URL or PGHOST is correct
- Check firewall/network settings

### Issue: "User not authenticated"
**Cause**: Session expired or not logged in
**Solution**:
- Clear browser cookies
- Login again
- Check that Spring Security is configured correctly

### Issue: H2 Console not available in production
**Expected**: H2 console only available in dev mode
**Solution**: This is by design - H2 is not used in production

---

## Switching Environments

### Local Development -> Production:
```bash
# Stop dev mode (Ctrl+C)
# Set production profile
export SPRING_PROFILES_ACTIVE=prod
# Set database connection
export PGHOST=localhost
export PGDATABASE=inventorydb
# Start production mode
mvn spring-boot:run
```

### Production -> Development:
```bash
# Stop prod mode (Ctrl+C)
# Set dev profile
export SPRING_PROFILES_ACTIVE=dev
# Start development mode
mvn spring-boot:run
```

---

## Security Considerations

### Development:
- H2 console exposed (only on localhost)
- Verbose logging enabled
- SQL queries logged
- Suitable for debugging only

### Production:
- H2 console disabled
- Minimal logging
- SSL required for database
- CSRF protection enabled
- Passwords encrypted with BCrypt
- Session-based authentication

---

## Performance Tuning

### Connection Pool (HikariCP):
```properties
# In application-prod.properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Adjust for your needs:
- **High traffic**: Increase maximum-pool-size to 20-50
- **Low traffic**: Decrease to 5-10 to save resources
- **Slow queries**: Increase connection-timeout

---

## Backup and Restore

### Development (H2):
- No backup needed (in-memory)
- Data lost on restart
- Good for testing

### Production (PostgreSQL):
**Render:**
- Automatic backups enabled by default
- Restore via Render dashboard

**Railway:**
- Automatic backups available on paid plans
- Use pg_dump for manual backups:
```bash
railway run pg_dump > backup.sql
```

---

## Getting Help

- **Build issues**: Check Maven output for errors
- **Runtime issues**: Check application logs
- **Database issues**: Check DatabaseConfig logs (look for "=== DATABASE CONFIGURATION DEBUG ===")
- **Connection issues**: Verify environment variables with `env | grep PG` or `env | grep DATABASE`
