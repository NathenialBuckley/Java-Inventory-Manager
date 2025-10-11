# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot REST API for inventory management built with Java 11. The application provides CRUD operations for inventory items using Spring Data JPA with an H2 in-memory database.

## Build System & Commands

This is a Maven project using Spring Boot 2.7.14.

### Build and Run
```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Run a single test
mvn test -Dtest=InventoryApiIntegrationTest
mvn test -Dtest=InventoryApiIntegrationTest#basicCreateAndGet
```

### Application Access
- API base URL: http://localhost:8080/api/items
- H2 Console: http://localhost:8080/h2-console (enabled in application.properties)
  - JDBC URL: jdbc:h2:mem:inventorydb
  - Driver: org.h2.Driver

## Architecture

The application follows a standard 3-layer Spring Boot architecture:

**Controller Layer** (`controller/`)
- `InventoryController` - REST endpoints at `/api/items`
- Uses constructor injection for the service layer
- Returns `ResponseEntity` objects with appropriate HTTP status codes

**Service Layer** (`service/`)
- `InventoryService` - Business logic and validation
- Validates required fields (name, sku) in the `create()` method
- Throws `IllegalArgumentException` for validation failures and missing entities

**Repository Layer** (`repository/`)
- `ItemRepository` - JPA repository extending `JpaRepository<Item, Long>`
- Includes custom query method `findBySku(String sku)` (defined but not currently used in service layer)

**Model** (`model/`)
- `Item` - JPA entity mapped to "items" table
- Fields: id (Long, auto-generated), name, sku, quantity (Integer), price (BigDecimal)

## Database Configuration

The application uses an H2 in-memory database configured in `src/main/resources/application.properties`:
- Database is recreated on each startup
- JPA is set to `hibernate.ddl-auto=update` which auto-creates tables from entities
- No manual schema migrations required

## Testing

Integration tests use `@SpringBootTest` with `TestRestTemplate` to test the full REST API stack. Tests run on a random port to avoid conflicts.
