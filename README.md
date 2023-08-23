# api

## Usage

```shell
./gradlew quarkusDev --quarkus-args="run-api"
```

### Generating a token

Prerequisite: have the private key at `src/main/resource/privateKey.pem` (yeah I know)

```shell
./gradlew quarkusDev --quarkus-args="generate-token <service-name> <absolute-output-path>"
```
