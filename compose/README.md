# EOSC Node Endpoint — Docker Compose

> **This is an example deployment.** The files in this directory are a starting point — review and adjust all configuration, credentials, and security settings before using in production.

Runs the backend service. This is not a complete production deployment — in production, deploy this service together with the front-end and a reverse proxy or ingress in an environment-specific stack.

## Prerequisites

- Docker with the Compose plugin
- Access to `docker.madgik.di.uoa.gr` (or a locally built image — see below)

## Setup

### 1. User/group identity

Copy the example file to the compose directory (needs to be in the same directory as `compose.yaml`):

```bash
cp compose/.env.example compose/.env
```

Edit `compose/.env` and set `HOST_UID`/`HOST_GID` to your host user's IDs (run `id -u` and `id -g` to get them). This ensures files written by the container are owned by you. If your UID/GID is already 1000 you can skip this step.

### 2. Application config

Copy the example file to the `compose/config` directory:

```bash
cp compose/config/application.properties.example compose/config/application.properties
```

Edit `compose/config/application.properties` and fill in all deployment-specific values: OAuth2 issuer URI, client credentials, admin emails, and redirect URLs.

`application.properties` is mounted as a Docker secret and is never exposed as an environment variable.

## Running through Makefile

### Using Docker

```bash
# Build image and start
make docker-build
make docker-compose

# Stop and remove containers
make docker-compose-down
```

### Running the JAR

```bash
# Build jar and start
make build
make run
```
`make run` runs the Spring Boot JAR directly on the host. By default it loads `compose/config/application.properties` and overrides `capabilities.filepath` to the host-relative path. Override with `CONFIG=...` to point at a different config file:

```bash
make run CONFIG=file:/path/to/local-application.properties
```

## Directory structure

```
compose/
├── compose.yaml
├── .env                               # local UID/GID (gitignored)
├── .env.example                       # template — copy to .env
└── config/
    ├── application.properties         # Spring config with real values (gitignored)
    ├── application.properties.example # template — copy to application.properties
    └── capabilities.json              # initial capabilities state (tracked)
```

## Notes

- `application.properties` is mounted as a Docker secret (not a volume). Its contents are not visible via `docker inspect`.
- `capabilities.json` is mounted as a regular volume. The service writes updated capability state to this file on the host.
- The published port `127.0.0.1:8080` is for host access only. A reverse proxy or another container in the same Compose network should use the service name and container port: `http://endpoint:8080`.

## Known Issues

- Paketo buildpack image builds may fail with Docker Engine `29.5.1`; see [spring-projects/spring-boot#50470](https://github.com/spring-projects/spring-boot/issues/50470).
