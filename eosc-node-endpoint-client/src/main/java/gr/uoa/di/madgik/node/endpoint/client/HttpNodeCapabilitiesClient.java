/*
 * Copyright 2026 OpenAIRE AMKE & Athena Research and Innovation Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.node.endpoint.client;

import gr.uoa.di.madgik.node.capabilities.model.NodeCapabilities;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * Java HTTP client implementation for the endpoint capabilities API.
 *
 * <p>The configured URI should point to the endpoint resource itself, for example
 * {@code https://node.eosc-beyond.eu/api/endpoint}.</p>
 */
public class HttpNodeCapabilitiesClient implements NodeCapabilitiesClient {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_JSON_SUFFIX = "application/*+json";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_JSON = "text/json";

    private final URI endpointUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;

    public HttpNodeCapabilitiesClient(URI endpointUri) {
        this(builder(endpointUri));
    }

    private HttpNodeCapabilitiesClient(Builder builder) {
        this.endpointUri = builder.endpointUri;
        this.httpClient = builder.httpClient;
        this.objectMapper = builder.objectMapper;
        this.requestTimeout = builder.requestTimeout;
    }

    public static Builder builder(URI endpointUri) {
        return new Builder(endpointUri);
    }

    @Override
    public NodeCapabilities get() {
        HttpRequest request = applyCommonHeaders(HttpRequest.newBuilder(endpointUri))
                .GET()
                .build();

        return send(request);
    }

    @Override
    public NodeCapabilities update(NodeCapabilities capabilities, String accessToken) {
        Objects.requireNonNull(capabilities, "capabilities");
        String authorizationToken = requireAccessToken(accessToken);

        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(capabilities);
        } catch (JacksonException e) {
            throw new NodeClientException("Could not serialize endpoint capabilities", e);
        }

        HttpRequest request = applyCommonHeaders(HttpRequest.newBuilder(endpointUri), authorizationToken)
                .header("Content-Type", APPLICATION_JSON)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        return send(request);
    }

    private String requireAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken must not be blank for update requests");
        }
        return accessToken;
    }

    private NodeCapabilities send(HttpRequest request) {
        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NodeClientException("Endpoint capabilities request was interrupted", e);
        } catch (IOException e) {
            throw new NodeClientException("Could not call endpoint capabilities API", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new NodeClientException(
                    "Endpoint capabilities API returned HTTP " + response.statusCode(),
                    response.statusCode(),
                    new String(response.body(), StandardCharsets.UTF_8));
        }

        try {
            return objectMapper.readValue(response.body(), NodeCapabilities.class);
        } catch (JacksonException e) {
            throw new NodeClientException("Could not deserialize endpoint capabilities response", e);
        }
    }

    private HttpRequest.Builder applyCommonHeaders(HttpRequest.Builder builder) {
        return applyCommonHeaders(builder, null);
    }

    private HttpRequest.Builder applyCommonHeaders(HttpRequest.Builder builder, String accessToken) {
        builder.timeout(requestTimeout)
                .header("Accept", String.join(", ",
                        APPLICATION_JSON,
                        APPLICATION_JSON_SUFFIX,
                        APPLICATION_OCTET_STREAM,
                        TEXT_PLAIN,
                        TEXT_JSON));

        if (accessToken != null && !accessToken.isBlank()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        return builder;
    }

    public static class Builder {

        private final URI endpointUri;
        private HttpClient httpClient = HttpClient.newHttpClient();
        private ObjectMapper objectMapper = new ObjectMapper();
        private Duration requestTimeout = Duration.ofSeconds(30);

        private Builder(URI endpointUri) {
            this.endpointUri = Objects.requireNonNull(endpointUri, "endpointUri");
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        public HttpNodeCapabilitiesClient build() {
            return new HttpNodeCapabilitiesClient(this);
        }
    }
}
