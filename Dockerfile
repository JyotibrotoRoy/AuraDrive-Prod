FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Build the application (skip tests to speed up deployment)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Spring Boot's default)
EXPOSE 8080

# The command to start the application
ENTRYPOINT ["java", "-jar", "app.jar"]