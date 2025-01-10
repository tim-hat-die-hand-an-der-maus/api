FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

USER root
RUN microdnf install findutils && microdnf clean all
USER quarkus

COPY --chown=quarkus:quarkus . /code/

WORKDIR /code

RUN ./mvnw package -Dnative -Dmaven.test.skip

FROM quay.io/quarkus/quarkus-micro-image:2.0

USER 1000
WORKDIR /app
COPY --from=build --chown=1000:root --chmod=555 /code/target/*-runner /app/application
EXPOSE 8080

ARG APP_VERSION
ENV QUARKUS_LOG_SENTRY_RELEASE=$APP_VERSION

ENTRYPOINT [ "./application", "-Xmx128M"]
CMD [ "run-api" ]
