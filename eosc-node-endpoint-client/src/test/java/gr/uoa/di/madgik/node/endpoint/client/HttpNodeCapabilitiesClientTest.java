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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpNodeCapabilitiesClientTest {

    private static final URI ENDPOINT_URI = URI.create("https://node.eosc-beyond.eu/api/endpoint");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getFetchesEndpointCapabilities() throws IOException {
        CapturingHttpClient httpClient = new CapturingHttpClient(200, objectMapper.writeValueAsString(endpointCapabilities()));

        NodeCapabilities result = HttpNodeCapabilitiesClient.builder(ENDPOINT_URI)
                .httpClient(httpClient)
                .build()
                .get();

        assertEquals("GET", httpClient.request.method());
        assertEquals(ENDPOINT_URI, httpClient.request.uri());
        assertEquals("application/json, application/*+json, application/octet-stream, text/plain, text/json",
                httpClient.request.headers().firstValue("Accept").orElseThrow());
        assertEquals(URI.create("https://node.eosc-beyond.eu"), result.getNodeEndpoint());
        assertEquals("metadata", result.getCapabilities().getFirst().getCapabilityType());
    }

    @Test
    void updateSendsPayloadAndBearerToken() throws IOException {
        CapturingHttpClient httpClient = new CapturingHttpClient(200, objectMapper.writeValueAsString(endpointCapabilities()));

        NodeCapabilities result = HttpNodeCapabilitiesClient.builder(endpointUri())
                .httpClient(httpClient)
                .build()
                .update(endpointCapabilities(), "test-token");

        assertEquals("PUT", httpClient.request.method());
        assertEquals("Bearer test-token", httpClient.request.headers().firstValue("Authorization").orElseThrow());
        assertEquals("application/json", httpClient.request.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("metadata", result.getCapabilities().getFirst().getCapabilityType());
    }

    @Test
    void updateRequiresBearerToken() {
        CapturingHttpClient httpClient = new CapturingHttpClient(200, "{}");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> HttpNodeCapabilitiesClient.builder(endpointUri())
                        .httpClient(httpClient)
                        .build()
                        .update(endpointCapabilities(), " "));

        assertEquals("accessToken must not be blank for update requests", exception.getMessage());
        assertNull(httpClient.request);
    }

    @Test
    void nonSuccessResponseRaisesClientException() throws IOException {
        CapturingHttpClient httpClient = new CapturingHttpClient(403, "forbidden");

        NodeClientException exception = assertThrows(
                NodeClientException.class,
                () -> HttpNodeCapabilitiesClient.builder(ENDPOINT_URI)
                        .httpClient(httpClient)
                        .build()
                        .get());

        assertEquals(403, exception.getStatusCode());
        assertEquals("forbidden", exception.getResponseBody());
    }

    private URI endpointUri() {
        return ENDPOINT_URI;
    }

    private NodeCapabilities endpointCapabilities() {
        Capability capability = new Capability();
        capability.setCapabilityType("metadata");
        capability.setEndpoint("https://node.eosc-beyond.eu/api/metadata");

        NodeCapabilities capabilities = new NodeCapabilities();
        capabilities.setNodeEndpoint(URI.create("https://node.eosc-beyond.eu"));
        capabilities.setCapabilities(List.of(capability));
        return capabilities;
    }

    private static class CapturingHttpClient extends HttpClient {

        private final int statusCode;
        private final String responseBody;
        private HttpRequest request;

        private CapturingHttpClient(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.request = request;
            @SuppressWarnings("unchecked")
            T body = (T) responseBody.getBytes(StandardCharsets.UTF_8);
            return new SimpleHttpResponse<>(request, statusCode, body);
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
