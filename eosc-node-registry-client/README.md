# EOSC Node Registry Client

Small Java HTTP client for resolving EOSC Node Registry entries and enriching them with endpoint capabilities.

## Maven

```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-registry-client</artifactId>
  <version>1.0.1-SNAPSHOT</version>
</dependency>
```

The client depends on `eosc-node-endpoint-client`.

## Usage

```java
NodeRegistryClient client = new HttpNodeRegistryClient(
    URI.create("https://registry.example.org/nodes"),
    apiKey);

List<RegisteredNode> nodes = client.fetchNodes();
```

`fetchNodes()` calls the registry with `x-api-key` and returns registry nodes enriched with endpoint capabilities when their endpoint is reachable.
