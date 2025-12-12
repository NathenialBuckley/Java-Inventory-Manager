# Build stage
FROM maven:3
FROM maven:3.9-openjdk-21-slim AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/inventory-manager-1.0-SNAPSHOT.jar app.jar

# Expose port (Render will override with PORT env variable)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
