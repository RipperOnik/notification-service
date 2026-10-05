FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /app

# 1. Copy only Maven POM files first
COPY pom.xml .
COPY notification-domain/pom.xml notification-domain/pom.xml
COPY notification-application/pom.xml notification-application/pom.xml
COPY notification-infrastructure/pom.xml notification-infrastructure/pom.xml
COPY notification-dispatcher/pom.xml notification-dispatcher/pom.xml


# 2. Download Maven dependencies
RUN mvn dependency:go-offline \
    -pl notification-dispatcher \
    -am

# 3. Now copy actual source code
COPY notification-domain/src notification-domain/src
COPY notification-application/src notification-application/src
COPY notification-infrastructure/src notification-infrastructure/src
COPY notification-dispatcher/src notification-dispatcher/src

# 4. Compile/package
RUN mvn package \
    -pl notification-dispatcher \
    -am \
    -DskipTests


FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build \
     /app/notification-dispatcher/target/*.jar \
     app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]