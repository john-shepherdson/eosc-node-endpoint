[![EOSC Beyond Logo][eosc-logo]]()

# EOSC Node Endpoint

Multi-module Java project for publishing and consuming EOSC Node endpoint capabilities.

## Modules

| Module | Description |
|--------|-------------|
| [`eosc-node-capabilities-model`](eosc-node-capabilities-model) | Reusable DTOs and recommended protocol/status values |
| [`eosc-node-endpoint-client`](eosc-node-endpoint-client) | Java HTTP client for the endpoint capabilities API |
| [`eosc-node-registry-client`](eosc-node-registry-client) | Java HTTP client for resolving Node Registry entries |
| [`eosc-node-endpoint-service`](eosc-node-endpoint-service) | Spring Boot service that stores and exposes endpoint capabilities |

## Prerequisites

* Java 21
* Maven 3.9+
* Docker, for image builds and Compose deployment

## Build

```bash
./mvnw clean package
```

To install all modules into your local Maven repository:

```bash
./mvnw clean install
```

## Service Quick Start

Run the packaged service:

```bash
java -jar eosc-node-endpoint-service/target/eosc-node-endpoint-service-<version>.jar \
  --spring.config.additional-location=file:/path/to/application.yaml
```

Build and run with Docker Compose:

```bash
make docker-build
make docker-compose
```

Before running, populate `compose/config/.env` with the required secrets (see `compose/config/application.properties` for the expected variable names).

See [`eosc-node-endpoint-service`](eosc-node-endpoint-service) for configuration, authentication, and API details.

## Published Artifacts

Other Java projects can depend on the reusable modules:

<!-- x-release-please-start-version -->
```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-capabilities-model</artifactId>
  <version>1.1.0</version>
</dependency>
```
<!-- x-release-please-end -->
<!-- x-release-please-start-version -->
```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-endpoint-client</artifactId>
  <version>1.1.0</version>
</dependency>
```
<!-- x-release-please-end -->
<!-- x-release-please-start-version -->
```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-registry-client</artifactId>
  <version>1.1.0</version>
</dependency>
```
<!-- x-release-please-end -->

The Docker image repository for the service is:

```text
docker.madgik.di.uoa.gr/eosc-node-endpoint-service
```

[eosc-logo]: https://eosc.eu/wp-content/uploads/2024/02/EOSC-Beyond-logo.png
