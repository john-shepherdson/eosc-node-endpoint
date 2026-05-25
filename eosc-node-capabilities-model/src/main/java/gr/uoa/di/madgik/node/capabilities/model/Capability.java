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

package gr.uoa.di.madgik.node.capabilities.model;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes a single capability exposed by a node endpoint.
 *
 * <p>A capability includes the capability type, the concrete service endpoint, an optional
 * version, an optional API specification location, and metadata about the communication
 * protocol and operational status.</p>
 *
 * <p>The {@code protocol} and {@code status} fields are intentionally stored as {@link String}
 * values to preserve extensibility. Known values are documented by {@link Protocol} and
 * {@link Status}, but values outside those enums are still accepted and normalized when set.</p>
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Capability {

    private String capabilityType;
    private URI endpoint;
    private String version;
    private URI apiSpec;
    private String protocol = Protocol.REST.value();
    private String status = Status.OPERATIONAL.value();

    public Capability() {
    }

    public String getCapabilityType() {
        return capabilityType;
    }

    public void setCapabilityType(String capabilityType) {
        this.capabilityType = capabilityType;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint == null ? null : URI.create(endpoint);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public URI getApiSpec() {
        return apiSpec;
    }

    public void setApiSpec(URI apiSpec) {
        this.apiSpec = apiSpec;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        if (protocol == null) {
            this.protocol = null;
            return;
        }

        Optional<Protocol> p = Protocol.fromValue(protocol);
        if (p.isPresent()) {
            this.protocol = p.get().value();
        } else {
            this.protocol = protocol.trim().toUpperCase(Locale.ROOT);
        }
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = Objects.requireNonNull(protocol, "protocol").value();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null) {
            this.status = null;
            return;
        }

        Optional<Status> s = Status.fromValue(status);
        if (s.isPresent()) {
            this.status = s.get().value();
        } else {
            this.status = status.trim().toUpperCase(Locale.ROOT);
        }
    }

    public void setStatus(Status status) {
        this.status = Objects.requireNonNull(status, "status").value();
    }

}
