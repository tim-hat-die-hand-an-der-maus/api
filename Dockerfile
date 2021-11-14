FROM quay.io/quarkus/ubi-quarkus-mandrel:21.3-java17 as build

COPY --chown=quarkus:quarkus . /code/

USER quarkus

WORKDIR /code

RUN ./gradlew buildNative

FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /app
COPY --from=build /code/build/*-runner /app/application
RUN chmod 755 /app
EXPOSE 8080

ARG build
ENV BUILD=$build

CMD [ "./application", "-Dquarkus.http.host=0.0.0.0" ]
