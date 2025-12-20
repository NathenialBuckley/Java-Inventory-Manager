# Java Inventory Manager

A full-stack Spring Boot inventory management system with user authentication, transaction tracking, and multi-environment support. Built with Java 21, Spring Boot 2.7.14, PostgreSQL (production), and H2 (development).

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database](#database)
- [API Endpoints](#api-endpoints)
- [Build and Run](#build-and-run)
- [Deployment](#deployment)
- [Project Structure](#project-structure)

## Features

### Build and Run

```bash
# Build the project
mvn clean package

# Run in DEVELOPMENT mode (H2 in-memory database)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run in PRODUCTION mode (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Or set environment variable
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

### 💰 Transaction Tracking
- **Buy transactions** - Record inventory purchases with automatic quantity increase
- **Sell transactions** - Process sales with automatic quantity decrease and stock validation
- Complete audit trail with before/after inventory snapshots
- Transaction metadata: date, price per unit, total amount, status, and optional notes
- Prevent overselling with inventory availability checks

### 📊 Dashboard & Analytics
- Real-time inventory metrics:
  - Total items count
  - Total inventory value
  - Total quantity across all items
  - Low stock alerts (items below threshold)
- Transaction analytics:
  - Total transaction count
  - Total spending (buy transactions)
  - Total sales revenue (sell transactions)
  - Net profit/loss calculation
- Recent activity feed with latest transactions
- Top value items by total inventory value
- Low stock items requiring attention

### 🔍 Advanced Querying
- Transaction history by user
- Transaction history by item
- Financial summaries and reports
- Date-ordered transaction lists

## Technology Stack

### Core Framework
- **Java 17** - LTS version with modern language features
- **Spring Boot 2.7.14** - Production-ready application framework
- **Maven** - Build automation and dependency management

### Spring Ecosystem
- **Spring Web** - REST API endpoints and HTTP handling
- **Spring Data JPA** - Database abstraction and ORM
- **Spring Security** - Authentication, authorization, and security
- **Hibernate** - JPA implementation and ORM provider

### Database
- **PostgreSQL** - Production database (Render deployment)
- **H2** - In-memory database for testing
- **HikariCP** - High-performance JDBC connection pooling

### Security
- **BCrypt** - Password hashing algorithm
- **Spring Security** - Framework for authentication and authorization

## Architecture

The application follows a clean, layered architecture pattern:

```
┌─────────────────────────────────────────────┐
│           Controller Layer                  │
│  (REST endpoints, request/response)         │
├─────────────────────────────────────────────┤
│            Service Layer                    │
│  (Business logic, validation)               │
├─────────────────────────────────────────────┤
│          Repository Layer                   │
│  (Data access, JPA repositories)            │
├─────────────────────────────────────────────┤
│            Model Layer                      │
│  (JPA entities, domain objects)             │
└─────────────────────────────────────────────┘
```

### Controller Layer
- **AuthController** - User registration and authentication (`/api/auth`)
- **InventoryController** - Item CRUD operations (`/api/items`)
- **TransactionController** - Buy/sell transactions (`/api/transactions`)
- **DashboardController** - Analytics and statistics (`/api/dashboard`)

### Service Layer
- **UserDetailsServiceImpl** - Spring Security user authentication
- **InventoryService** - Item management business logic
- **TransactionService** - Transaction processing with inventory updates
- **DashboardService** - Dashboard metrics and analytics

### Repository Layer
- **UserRepository** - User data access with custom queries
- **ItemRepository** - Item data access with user filtering
- **TransactionRepository** - Transaction data access with aggregation queries

### Model Layer
- **User** - User accounts with roles and credentials
- **Item** - Inventory items with quantity and pricing
- **Transaction** - Buy/sell transactions with audit trail
- **TransactionType** - Enum: BUY, SELL
- **TransactionStatus** - Enum: PENDING, COMPLETED, FAILED, REVERSED

## Database

### Configuration
The application uses environment-aware database configuration:

**Production (Render):**
- PostgreSQL database via `DATABASE_URL` environment variable
- SSL-enabled connections
- Automatic schema creation via Hibernate

**Local Development:**
- PostgreSQL with individual environment variables (PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE)
- Configurable schema via `DB_SCHEMA` environment variable (defaults to `public`)

### Schema
Three main tables with relationships:

```sql
users
├── id (PK)
├── username (unique)
├── password (BCrypt hashed)
├── role
├── enabled
└── created_at

items
├── id (PK)
├── name
├── sku
├── quantity
├── price
└── user_id (FK -> users)

transactions
├── id (PK)
├── item_id (FK -> items)
├── user_id (FK -> users)
├── type (BUY/SELL)
├── status
├── quantity
├── price_per_unit
├── total_amount
├── transaction_date
├── inventory_before
├── inventory_after
└── notes
```

## API Endpoints

### Authentication
```
POST   /api/auth/register     - Register new user
POST   /api/auth/login        - Login (form-based)
POST   /api/auth/logout       - Logout
GET    /api/auth/current      - Get current user info
POST   /api/auth/check        - Check authentication status
```

### Inventory Management
```
GET    /api/items             - List all items (user-specific)
GET    /api/items/{id}        - Get item by ID
POST   /api/items             - Create new item
PUT    /api/items/{id}        - Update item
DELETE /api/items/{id}        - Delete item
```

### Transactions
```
GET    /api/transactions                - List all transactions (user-specific)
POST   /api/transactions                - Create buy/sell transaction
GET    /api/transactions/item/{itemId}  - Get transactions for specific item
GET    /api/transactions/summary        - Get financial summary
```

### Dashboard
```
GET    /api/dashboard         - Get comprehensive dashboard statistics
```

## Build and Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (for local development)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Java-Inventory-Manager
   ```

2. **Set up PostgreSQL database**
   ```bash
   createdb inventorydb
   ```

3. **Configure environment variables** (optional)
   ```bash
   export PGHOST=localhost
   export PGPORT=5432
   export PGUSER=your_username
   export PGPASSWORD=your_password
   export PGDATABASE=inventorydb
   export DB_SCHEMA=public  # or custom schema name
   ```

4. **Build the project**
   ```bash
   mvn clean package
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or with environment variables:
   ```bash
   PGPASSWORD=your_password mvn spring-boot:run
   ```

6. **Run tests**
   ```bash
   mvn test
   ```

The application will start on `http://localhost:8080`

### Quick Test
```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'

# Create an item
curl -X POST http://localhost:8080/api/items \
  -u demo:demo123 \
  -H "Content-Type: application/json" \
  -d '{"name":"Widget","sku":"WG-001","quantity":100,"price":25.50}'

# Make a buy transaction
curl -X POST http://localhost:8080/api/transactions \
  -u demo:demo123 \
  -H "Content-Type: application/json" \
  -d '{"itemId":1,"type":"BUY","quantity":50,"pricePerUnit":20.00}'
```

## Deployment

### Render Deployment
The application is configured for deployment on Render:

1. **Environment Variables:**
   - `DATABASE_URL` - Automatically provided by Render PostgreSQL
   - `PORT` - Automatically provided by Render (defaults to 8080)

2. **Build Command:**
   ```bash
   mvn clean package
   ```

3. **Start Command:**
   ```bash
   java -jar target/inventory-manager-1.0-SNAPSHOT.jar
   ```

### Database Configuration
The `DatabaseConfig` class automatically detects the deployment environment:
- On Render: Parses `DATABASE_URL` and configures PostgreSQL with SSL
- Locally: Uses individual environment variables from `application.properties`

## Project Structure

```
src/
├── main/
│   ├── java/dev/inventorymanager/
│   │   ├── config/              # Configuration classes
│   │   │   ├── DatabaseConfig.java    # Multi-environment DB config
│   │   │   └── SecurityConfig.java    # Spring Security setup
│   │   ├── controller/          # REST API controllers
│   │   │   ├── AuthController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── InventoryController.java
│   │   │   └── TransactionController.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   └── DashboardResponse.java
│   │   ├── model/               # JPA entities
│   │   │   ├── Item.java
│   │   │   ├── Transaction.java
│   │   │   ├── TransactionStatus.java
│   │   │   ├── TransactionType.java
│   │   │   └── User.java
│   │   ├── repository/          # Data access layer
│   │   │   ├── ItemRepository.java
│   │   │   ├── TransactionRepository.java
│   │   │   └── UserRepository.java
│   │   └── service/             # Business logic layer
│   │       ├── DashboardService.java
│   │       ├── InventoryService.java
│   │       ├── TransactionService.java
│   │       └── UserDetailsServiceImpl.java
│   └── resources/
│       └── application.properties     # Application configuration
└── test/
    └── java/dev/inventorymanager/
        └── InventoryApiIntegrationTest.java
```

## Key Design Decisions

### Multi-Tenancy
- User isolation implemented at the repository level
- All queries automatically filter by the authenticated user
- Prevents data leakage between users

### Transaction Atomicity
- All buy/sell operations are transactional (`@Transactional`)
- Inventory updates and transaction records committed together
- Automatic rollback on failure ensures data consistency

### Audit Trail
- Every transaction captures inventory snapshots (before/after)
- Immutable transaction records for compliance
- Complete history for debugging and analytics

### Security
- Passwords never stored in plain text (BCrypt hashing)
- CSRF protection disabled for REST API compatibility
- HTTP Basic Auth for API access
- Form-based login for web interface

### Validation
- Business logic validation in service layer
- Database constraints for data integrity
- Prevent negative inventory with stock validation
- Required field validation for all entities

## License

This project is part of a portfolio demonstrating full-stack Java development skills with Spring Boot.
