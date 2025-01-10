FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

USER root
RUN microdnf install findutils && microdnf clean all
USER quarkus

COPY --chown=quarkus:quarkus . /code/

WORKDIR /code

RUN ./gradlew assemble -Dquarkus.native.enabled=true

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app
COPY --from=build /code/build/*-runner /app/application
RUN chmod 755 /app
EXPOSE 8080

ARG APP_VERSION
ENV QUARKUS_LOG_SENTRY_RELEASE=$APP_VERSION

ENTRYPOINT [ "./application", "-Xmx128M"]
CMD [ "run-api" ]
