# EOSC Node Capabilities Model

Reusable Java model classes for EOSC Node endpoint capability documents.

## Maven

<!-- x-release-please-start-version -->
```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-capabilities-model</artifactId>
  <version>1.1.0</version>
</dependency>
```
<!-- x-release-please-end -->

## Contents

The module contains:

| Type | Purpose |
|------|---------|
| `NodeCapabilities` | Full capability document for one node endpoint |
| `Capability` | One advertised endpoint capability |
| `Protocol` | Recommended protocol values |
| `Status` | Recommended operational status values |

Request and response JSON uses `snake_case` through Jackson annotations:

```json
{
  "node_endpoint": "https://node.eosc-beyond.eu",
  "capabilities": [
    {
      "capability_type": "metadata",
      "endpoint": "https://node.eosc-beyond.eu/api/metadata",
      "version": "1.0.0",
      "api_spec": "https://node.eosc-beyond.eu/api/openapi.json",
      "protocol": "REST",
      "status": "OPERATIONAL"
    }
  ]
}
```

`protocol` and `status` are extensible string fields. Known values are canonicalized, and unknown values are accepted and normalized to uppercase.

Recommended protocols: `REST`, `SOAP`, `gRPC`, `SSE`, `WebSocket`, `RSocket`.

Recommended statuses: `OPERATIONAL`, `MAINTENANCE`, `UNAVAILABLE`.
