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
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpNodeRegistryClientTest {

    private static final URI REGISTRY_URI = URI.create("https://registry.example.org/nodes");
    private static final URI NODE_ENDPOINT = URI.create("https://node.example.org/api/endpoint");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void fetchNodesLoadsRegistryNodesAndEnrichesCapabilities() throws IOException {
        CapturingHttpClient httpClient = new CapturingHttpClient(
                new QueuedResponse(200, objectMapper.writeValueAsString(registryNodes())),
                new QueuedResponse(200, objectMapper.writeValueAsString(endpointCapabilities())));

        List<Node> result = HttpNodeRegistryClient.builder(REGISTRY_URI, "test-key")
                .httpClient(httpClient)
                .objectMapper(objectMapper)
                .build()
                .fetchNodes();

        assertEquals(2, httpClient.requests.size());
        assertEquals(REGISTRY_URI, httpClient.requests.get(0).uri());
        assertEquals("test-key", httpClient.requests.get(0).headers().firstValue("x-api-key").orElseThrow());
        assertEquals(NODE_ENDPOINT, httpClient.requests.get(1).uri());
        assertEquals("Test Node", result.getFirst().getName());
        assertEquals("metadata", result.getFirst().getCapabilities().getFirst().getCapabilityType());
    }

    @Test
    void capabilityFailuresProduceEmptyCapabilityList() throws IOException {
        CapturingHttpClient httpClient = new CapturingHttpClient(
                new QueuedResponse(200, objectMapper.writeValueAsString(registryNodes())),
                new QueuedResponse(500, "endpoint failed"));

        List<Node> result = HttpNodeRegistryClient.builder(REGISTRY_URI, "test-key")
                .httpClient(httpClient)
                .objectMapper(objectMapper)
                .build()
                .fetchNodes();

        assertTrue(result.getFirst().getCapabilities().isEmpty());
    }

    @Test
    void invalidNodeEndpointProducesEmptyCapabilityList() throws IOException {
        CapturingHttpClient httpClient = new CapturingHttpClient(
                new QueuedResponse(200, objectMapper.writeValueAsString(registryNodesWithEndpoint(
                        URI.create("ftp://node.example.org/api/endpoint")))));

        List<Node> result = HttpNodeRegistryClient.builder(REGISTRY_URI, "test-key")
                .httpClient(httpClient)
                .objectMapper(objectMapper)
                .build()
                .fetchNodes();

        assertEquals(1, httpClient.requests.size());
        assertTrue(result.getFirst().getCapabilities().isEmpty());
    }

    @Test
    void registryFailureRaisesClientException() {
        CapturingHttpClient httpClient = new CapturingHttpClient(new QueuedResponse(403, "forbidden"));

        NodeRegistryClientException exception = assertThrows(
                NodeRegistryClientException.class,
                () -> HttpNodeRegistryClient.builder(REGISTRY_URI, "test-key")
                        .httpClient(httpClient)
                        .build()
                        .fetchNodes());

        assertEquals(403, exception.getStatusCode());
        assertEquals("forbidden", exception.getResponseBody());
    }

    @Test
    void builderRequiresApiKey() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> HttpNodeRegistryClient.builder(REGISTRY_URI, " "));

        assertEquals("apiKey must not be blank", exception.getMessage());
    }

    private List<Node> registryNodes() {
        return registryNodesWithEndpoint(NODE_ENDPOINT);
    }

    private List<Node> registryNodesWithEndpoint(URI nodeEndpoint) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setName("Legal Entity");
        legalEntity.setRorId("https://ror.org/test");

        Node node = new Node();
        node.setId("node-id");
        node.setName("Test Node");
        node.setLogo(URI.create("https://node.example.org/logo.png"));
        node.setPid("node-pid");
        node.setLegalEntity(legalEntity);
        node.setNodeEndpoint(nodeEndpoint);
        return List.of(node);
    }

    private NodeCapabilities endpointCapabilities() {
        Capability capability = new Capability();
        capability.setCapabilityType("metadata");
        capability.setEndpoint("https://node.example.org/api/metadata");

        NodeCapabilities capabilities = new NodeCapabilities();
        capabilities.setNodeEndpoint(URI.create("https://node.example.org"));
        capabilities.setCapabilities(List.of(capability));
        return capabilities;
    }

    private record QueuedResponse(int statusCode, String body) {
    }

    private static class CapturingHttpClient extends HttpClient {

        private final Queue<QueuedResponse> responses;
        private final List<HttpRequest> requests = new java.util.ArrayList<>();

        private CapturingHttpClient(QueuedResponse... responses) {
            this.responses = new ArrayDeque<>(List.of(responses));
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            requests.add(request);
            QueuedResponse response = responses.remove();
            @SuppressWarnings("unchecked")
            T body = (T) response.body();
            return new SimpleHttpResponse<>(request, response.statusCode(), body);
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException("sendAsync is not used by this client");
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException("sendAsync is not used by this client");
        }
    }

    private record SimpleHttpResponse<T>(HttpRequest request, int statusCode, T body) implements HttpResponse<T> {

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
