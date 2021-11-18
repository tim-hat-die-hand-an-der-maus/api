FROM quay.io/quarkus/ubi-quarkus-native-image:21.3-java11 as build

COPY --chown=quarkus:quarkus . /code/

USER quarkus

WORKDIR /code

RUN ./gradlew assemble -Dquarkus.package.type=native

FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /app
COPY --from=build /code/build/*-runner /app/application
RUN chmod 755 /app
EXPOSE 8080

ARG build
ENV BUILD=$build

ENTRYPOINT [ "./application", "-Xmx128M"]

