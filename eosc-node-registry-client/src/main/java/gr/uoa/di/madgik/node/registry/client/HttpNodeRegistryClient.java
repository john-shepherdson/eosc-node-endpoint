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

package gr.uoa.di.madgik.node.registry.client;

import gr.uoa.di.madgik.node.capabilities.model.Capability;
import gr.uoa.di.madgik.node.capabilities.model.NodeCapabilities;
import gr.uoa.di.madgik.node.endpoint.client.HttpNodeCapabilitiesClient;
import gr.uoa.di.madgik.node.endpoint.client.NodeClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Java HTTP client implementation for resolving Node Registry entries.
 */
public class HttpNodeRegistryClient implements NodeRegistryClient {

    private static final String APPLICATION_JSON = "application/json";
    private static final String API_KEY_HEADER = "x-api-key";
    private static final System.Logger LOGGER = System.getLogger(HttpNodeRegistryClient.class.getName());

    private final URI registryUri;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration requestTimeout;
    private final Duration capabilitiesRequestTimeout;

    public HttpNodeRegistryClient(URI registryUri, String apiKey) {
        this(builder(registryUri, apiKey));
    }

    private HttpNodeRegistryClient(Builder builder) {
        this.registryUri = builder.registryUri;
        this.apiKey = builder.apiKey;
        this.httpClient = builder.httpClient;
        this.objectMapper = builder.objectMapper;
        this.requestTimeout = builder.requestTimeout;
        this.capabilitiesRequestTimeout = builder.capabilitiesRequestTimeout;
    }

    public static Builder builder(URI registryUri, String apiKey) {
        return new Builder(registryUri, apiKey);
    }

    @Override
    public List<Node> fetchNodes() {
        List<Node> nodes = fetchRegisteredNodes();
        return nodes.stream()
                .map(this::populateCapabilities)
                .toList();
    }

    private List<Node> fetchRegisteredNodes() {
        HttpRequest request = HttpRequest.newBuilder(registryUri)
                .timeout(requestTimeout)
                .header(API_KEY_HEADER, apiKey)
                .header("Accept", APPLICATION_JSON)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NodeRegistryClientException("Node Registry request was interrupted", e);
        } catch (IOException e) {
            throw new NodeRegistryClientException("Could not call Node Registry API", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new NodeRegistryClientException(
                    "Node Registry API returned HTTP " + response.statusCode(),
                    response.statusCode(),
                    response.body());
        }

        try {
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (JacksonException e) {
            throw new NodeRegistryClientException("Could not deserialize Node Registry response", e);
        }
    }

    private Node populateCapabilities(Node node) {
        node.setCapabilities(fetchCapabilities(node.getNodeEndpoint()));
        return node;
    }

    private List<Capability> fetchCapabilities(URI nodeEndpoint) {
        if (nodeEndpoint == null) {
            return List.of();
        }

        try {
            NodeCapabilities response = HttpNodeCapabilitiesClient.builder(nodeEndpoint)
                    .httpClient(httpClient)
                    .objectMapper(objectMapper)
                    .requestTimeout(capabilitiesRequestTimeout)
                    .build()
                    .get();

            if (response == null || response.getCapabilities() == null) {
                return List.of();
            }

            return response.getCapabilities();
        } catch (NodeClientException | IllegalArgumentException e) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to fetch capabilities from node endpoint " + nodeEndpoint,
                    e);
            return List.of();
        }
    }

    public static class Builder {

        private final URI registryUri;
        private final String apiKey;
        private HttpClient httpClient = HttpClient.newHttpClient();
        private ObjectMapper objectMapper = new ObjectMapper();
        private Duration requestTimeout = Duration.ofSeconds(30);
        private Duration capabilitiesRequestTimeout = Duration.ofSeconds(5);

        private Builder(URI registryUri, String apiKey) {
            this.registryUri = Objects.requireNonNull(registryUri, "registryUri");
            this.apiKey = requireApiKey(apiKey);
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

        public Builder capabilitiesRequestTimeout(Duration capabilitiesRequestTimeout) {
            this.capabilitiesRequestTimeout = Objects.requireNonNull(
                    capabilitiesRequestTimeout,
                    "capabilitiesRequestTimeout");
            return this;
        }

        public HttpNodeRegistryClient build() {
            return new HttpNodeRegistryClient(this);
        }

        private String requireApiKey(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("apiKey must not be blank");
            }
            return apiKey;
        }
    }
}
