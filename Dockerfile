FROM quay.io/quarkus/ubi-quarkus-native-image:22.3-java17 AS build

USER root

RUN microdnf install findutils && microdnf clean all

USER quarkus

COPY --chown=quarkus:quarkus . /code/

WORKDIR /code

RUN ./gradlew assemble -Dquarkus.package.type=native

FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /app
COPY --from=build /code/build/*-runner /app/application
RUN chmod 755 /app
EXPOSE 8080

ARG build
ENV QUARKUS_LOG_SENTRY_RELEASE=$build

ENTRYPOINT [ "./application", "-Xmx128M"]
CMD [ "run-api" ]
