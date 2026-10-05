FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /app

# 1. Copy only Maven POM files first
COPY pom.xml .
COPY notification-domain/pom.xml notification-domain/pom.xml
COPY notification-application/pom.xml notification-application/pom.xml
COPY notification-infrastructure/pom.xml notification-infrastructure/pom.xml
COPY notification-web/pom.xml notification-web/pom.xml


# 2. Download Maven dependencies
RUN mvn dependency:go-offline \
    -pl notification-web \
    -am

# 3. Now copy actual source code
COPY notification-domain/src notification-domain/src
COPY notification-application/src notification-application/src
COPY notification-infrastructure/src notification-infrastructure/src
COPY notification-web/src notification-web/src

# 4. Compile/package
RUN mvn package \
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
