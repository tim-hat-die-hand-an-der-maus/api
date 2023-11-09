FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-17 AS build

USER root
RUN microdnf install findutils && microdnf clean all
USER quarkus

COPY --chown=quarkus:quarkus . /code/

WORKDIR /code

RUN ./gradlew assemble -Dquarkus.package.type=native

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app
COPY --from=build /code/build/*-runner /app/application
RUN chmod 755 /app
EXPOSE 8080

ARG build
ENV QUARKUS_LOG_SENTRY_RELEASE=$build

ENTRYPOINT [ "./application", "-Xmx128M"]
CMD [ "run-api" ]
