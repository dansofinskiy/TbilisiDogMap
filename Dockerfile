FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml maven-settings.xml ./
COPY src ./src

RUN mvn -s maven-settings.xml -Dmaven.repo.local=/root/.m2/repository clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/tbilisi-dog-map-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
