# api

## Usage

```shell
./mvnw quarkus:dev -Dquarkus.args="run-api"
```

### Generating a token

Prerequisite: have the private key at `src/main/resources/privateKey.pem` (yeah I know)

```shell
./mvnw quarkus:dev -Dquarkus.args="generate-token <service-name> <absolute-output-path>"
```
