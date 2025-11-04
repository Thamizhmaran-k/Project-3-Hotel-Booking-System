# ---------- Stage 1: Build ----------
# Uses Maven on the Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run ----------
#
# *** THIS IS THE FIX ***
# We are switching to a known, valid, and lightweight JRE (Java Runtime Environment)
# from the same Temurin family as the build stage.
FROM eclipse-temurin:21-jre-jammy
#
# *** END FIX ***

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Render's default port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]