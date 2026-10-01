FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /app

COPY . .

RUN mvn clean package \
    -pl notification-web \
    -am \
    -DskipTests


FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build \
     /app/notification-web/target/*.jar \
     app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
