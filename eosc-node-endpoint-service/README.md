[![EOSC Beyond Logo][eosc-logo]]()

# EOSC Node Endpoint Service

Spring Boot service for registering and updating a node's capabilities.
Capabilities are stored in a `capabilities.json` file at your chosen location and the file is created automatically on first update.

## Run

Run the packaged service directly when you want to start the backend without Docker.

From the repository root:

Copy the example config and fill in the required values:

```bash
cp compose/config/application.properties.example /path/to/application.properties
```

Run the service using Java:
```bash
java -jar eosc-node-endpoint-service/target/eosc-node-endpoint-service-<version>.jar \
  --spring.config.additional-location=file:/path/to/application.properties
```

## Docker

See [Run with Docker Compose](../compose/README.md) for building the image and running the service using Docker Compose.

## Configuration

Common runtime properties:

| Property | Description | Default |
|----------|-------------|---------|
| `capabilities.filepath` | Path to the JSON storage file | `/tmp/capabilities.json` |
| `capabilities.cache.ttl` | Cache TTL for loaded file contents, using Spring duration syntax | `PT60S` |
| `server.port` | HTTP port | `8080` |
| `server.servlet.context-path` | Base path for all HTTP endpoints | `/api` |
| `server.servlet.session.cookie.name` | Name of the HTTP session cookie used by the OAuth2 login flow | `NE_SESSION` |
| `server.servlet.session.cookie.path` | Cookie path | `/` |
| `security.admin-emails` | Comma-separated list of email addresses granted admin access | (required) |
| `security.login-redirect` | Redirect URL after a successful OAuth2 login | (required) |
| `security.logout-redirect` | Redirect URL after logout | (required) |

Because the OAuth2 properties contain secrets, supply them via an external config file rather than inline flags.

Use [`compose/config/application.properties.example`](../compose/config/application.properties.example) as a starting point. A minimal config file looks like:

```properties
capabilities.filepath=/path/to/capabilities.json
security.admin-emails=admin@example.eu
security.login-redirect=https://node.eosc-beyond.eu/
security.logout-redirect=https://node.eosc-beyond.eu/
spring.security.oauth2.client.provider.eosc.issuer-uri=https://core-proxy.node.eosc-beyond.eu/auth/realms/core
spring.security.oauth2.client.registration.eosc.client-id=my-client-id
spring.security.oauth2.client.registration.eosc.client-secret=my-client-secret
```

`--spring.config.additional-location` merges the external file on top of the bundled defaults, so only the properties that differ need to be set.
Override `capabilities.filepath` for any deployment where the file must survive restarts.

Manual edits to `capabilities.json` are picked up after the cache TTL expires.
The TTL uses Spring Boot duration syntax, for example `PT60S`, `PT5M`, or `1m`.

### OAuth2 / EOSC AAI Properties

Required for authenticated requests:

| Property | Description |
|----------|-------------|
| `spring.security.oauth2.client.provider.eosc.issuer-uri` | EOSC AAI issuer URI, used to discover OIDC endpoints |
| `spring.security.oauth2.client.registration.eosc.client-id` | OAuth2 client ID |
| `spring.security.oauth2.client.registration.eosc.client-secret` | OAuth2 client secret |
| `spring.security.oauth2.client.registration.eosc.client-name` | Display name for the login button, default `EOSC` |
| `spring.security.oauth2.client.registration.eosc.redirect-uri` | OAuth2 redirect URI sent to the provider; defaults to `{baseUrl}/login/oauth2/code/{registrationId}`. Set this explicitly when the service is behind a reverse proxy and its public URL differs from its own base URL, e.g. `https://node.eosc-beyond.eu/api/login/oauth2/code/eosc` |
| `spring.security.oauth2.client.registration.eosc.scope` | Requested scopes, default `openid`, `email`, `profile`, `entitlements` |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | Issuer URI for JWT access token validation |

The OAuth2 login flow requires the `spring.security.oauth2.client.*` properties. Bearer-token requests also use the `eosc` client provider metadata to call the UserInfo endpoint and map the authenticated email to the `ADMIN` authority.

## Authentication

The service uses EOSC AAI as its identity provider and supports two authentication flows.

### OAuth2 Login

Navigating to a protected endpoint redirects the browser to the EOSC AAI login page. After a successful login the service creates a server-side session identified by the `NE_SESSION` cookie.

This flow requires the `spring.security.oauth2.client.*` properties to be set.

### Bearer Token

Obtain an access token from the EOSC AAI token endpoint and pass it in the `Authorization` header:

```bash
curl -X PUT http://localhost:8080/api/endpoint \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '...'
```

The service validates the token against the EOSC AAI JWKS using the issuer URI configured in `spring.security.oauth2.resourceserver.jwt.issuer-uri`, then calls the EOSC AAI UserInfo endpoint to resolve the user's email. No session is created for this flow.

Unauthenticated or unauthorized requests receive `401 Unauthorized` or `403 Forbidden`.

The `ADMIN` authority is currently granted by matching the authenticated user's email against the `security.admin-emails` configuration property.

## API

| Method | Endpoint | Auth required | Description |
|--------|----------|---------------|-------------|
| `GET` | `/api/endpoint` | No | Returns the currently stored capability document |
| `PUT` | `/api/endpoint` | Yes, `ADMIN` role | Replaces the stored capability document and returns the saved payload |

Request and response bodies use the model documented in [../eosc-node-capabilities-model](../eosc-node-capabilities-model).

### Example Update

```bash
curl -X PUT http://localhost:8080/api/endpoint \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "node_endpoint": "https://node.eosc-beyond.eu",
    "capabilities": [
      {
        "capability_type": "metadata",
        "endpoint": "https://node.eosc-beyond.eu/api/metadata",
        "version": "1.0.0",
        "api_spec": "https://node.eosc-beyond.eu/api/openapi.json",
        "protocol": "REST",
        "status": "OPERATIONAL"
      },
      {
        "capability_type": "custom-service",
        "endpoint": "https://node.eosc-beyond.eu/custom",
        "protocol": "REST",
        "status": "UNAVAILABLE"
      }
    ]
  }'
```

[eosc-logo]: https://eosc.eu/wp-content/uploads/2024/02/EOSC-Beyond-logo.png
