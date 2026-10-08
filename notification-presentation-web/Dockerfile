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

COPY notification-application-worker/pom.xml \
     notification-application-worker/pom.xml

COPY notification-infrastructure-persistence/pom.xml \
     notification-infrastructure-persistence/pom.xml

COPY notification-infrastructure-messaging-publisher/pom.xml \
     notification-infrastructure-messaging-publisher/pom.xml

COPY notification-infrastructure-messaging-listener/pom.xml \
     notification-infrastructure-messaging-listener/pom.xml

COPY notification-infrastructure-observability/pom.xml \
     notification-infrastructure-observability/pom.xml

COPY notification-infrastructure-email/pom.xml \
     notification-infrastructure-email/pom.xml

COPY notification-presentation-web/pom.xml \
     notification-presentation-web/pom.xml

COPY notification-presentation-dispatcher/pom.xml \
     notification-presentation-dispatcher/pom.xml

COPY notification-presentation-worker/pom.xml \
     notification-presentation-worker/pom.xml

# ------------------------------------------------
# 2. Download/cache dependencies for Web + deps
# ------------------------------------------------

RUN mvn dependency:go-offline \
    -pl notification-presentation-web \
    -am


# ------------------------------------------------
# 3. Copy ONLY source needed to build Web
# ------------------------------------------------

COPY notification-domain/src \
     notification-domain/src

COPY notification-application-common/src \
     notification-application-common/src

COPY notification-application-api/src \
     notification-application-api/src

COPY notification-infrastructure-persistence/src \
     notification-infrastructure-persistence/src

COPY notification-infrastructure-observability/src \
     notification-infrastructure-observability/src

COPY notification-presentation-web/src \
     notification-presentation-web/src


# ------------------------------------------------
# 4. Build Web and all required upstream modules
# ------------------------------------------------

RUN mvn package \
    -pl notification-presentation-web \
    -am \
    -DskipTests


# ------------------------------------------------
# 5. Runtime image
# ------------------------------------------------

FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build \
     /app/notification-presentation-web/target/*.jar \
     app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]