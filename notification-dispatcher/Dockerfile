FROM maven:3.9-eclipse-temurin-26 AS build

WORKDIR /app


# ------------------------------------------------
# 1. Copy Maven metadata for the ENTIRE reactor
# ------------------------------------------------

COPY pom.xml .

COPY notification-domain/pom.xml \
     notification-domain/pom.xml

COPY notification-application-common/pom.xml \
     notification-application-common/pom.xml

COPY notification-application-api/pom.xml \
     notification-application-api/pom.xml

COPY notification-application-dispatcher/pom.xml \
     notification-application-dispatcher/pom.xml

COPY notification-infrastructure-persistence/pom.xml \
     notification-infrastructure-persistence/pom.xml

COPY notification-infrastructure-messaging/pom.xml \
     notification-infrastructure-messaging/pom.xml

COPY notification-web/pom.xml \
     notification-web/pom.xml

COPY notification-dispatcher/pom.xml \
     notification-dispatcher/pom.xml


# ------------------------------------------------
# 2. Download/cache dispatcher dependencies
# ------------------------------------------------

RUN mvn dependency:go-offline \
    -pl notification-dispatcher \
    -am


# ------------------------------------------------
# 3. Copy ONLY source needed by Dispatcher
# ------------------------------------------------

COPY notification-domain/src \
     notification-domain/src

COPY notification-application-common/src \
     notification-application-common/src

COPY notification-application-dispatcher/src \
     notification-application-dispatcher/src

COPY notification-infrastructure-persistence/src \
     notification-infrastructure-persistence/src

COPY notification-infrastructure-messaging/src \
     notification-infrastructure-messaging/src

COPY notification-dispatcher/src \
     notification-dispatcher/src


# ------------------------------------------------
# 4. Build Dispatcher + upstream dependencies
# ------------------------------------------------

RUN mvn package \
    -pl notification-dispatcher \
    -am \
    -DskipTests


# ------------------------------------------------
# 5. Runtime image
# ------------------------------------------------

FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build \
     /app/notification-dispatcher/target/*.jar \
     app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]