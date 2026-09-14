# --- Build stage -------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Leverage Docker layer caching: dependencies only re-download when the
# pom.xml itself changes, not on every source edit.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src ./src
RUN ./mvnw -q clean package -DskipTests

# --- Run stage -----------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render/Railway/Fly.io inject PORT at runtime; application.properties reads
# it via ${PORT:8080}. EXPOSE is documentation only, not a hard binding.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
