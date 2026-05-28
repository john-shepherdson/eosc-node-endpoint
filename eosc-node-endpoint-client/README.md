# EOSC Node Endpoint Client

Small Java HTTP client for the EOSC Node endpoint capabilities API.

## Maven

<!-- x-release-please-start-version -->
```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-endpoint-client</artifactId>
  <version>1.1.0</version>
</dependency>
```
<!-- x-release-please-end -->

The client depends on `eosc-node-capabilities-model`.

## Usage

Configure the client with the full endpoint resource URI:

```java
NodeCapabilitiesClient client = new HttpNodeCapabilitiesClient(
    URI.create("https://node.eosc-beyond.eu/api/endpoint"));

NodeCapabilities capabilities = client.get();
client.update(capabilities, accessToken);
```

`get()` calls `GET /api/endpoint` and does not require authentication.

`update(capabilities, accessToken)` calls `PUT /api/endpoint` and sends:

```text
Authorization: Bearer <accessToken>
Content-Type: application/json
Accept: application/json
```

## Errors

The HTTP implementation raises `NodeClientException` when:

- the request cannot be sent
- the response status is not 2xx
- the response body cannot be deserialized

For non-2xx responses, the exception exposes `getStatusCode()` and `getResponseBody()`.
