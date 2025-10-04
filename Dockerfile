FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY .mvn ./.mvn
COPY mvnw pom.xml ./

RUN ./mvnw --no-transfer-progress dependency:resolve-plugins dependency:go-offline

COPY src/ src/

RUN ./mvnw --no-transfer-progress package -Dnative -Dmaven.test.skip

FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

USER 1000
WORKDIR /app
COPY --from=build --chown=1000:root --chmod=555 /project/target/*-runner /app/application
EXPOSE 8080

ARG APP_VERSION
ENV QUARKUS_LOG_SENTRY_RELEASE=$APP_VERSION

ENTRYPOINT [ "./application", "-Xmx128M"]
CMD [ "run-api" ]
