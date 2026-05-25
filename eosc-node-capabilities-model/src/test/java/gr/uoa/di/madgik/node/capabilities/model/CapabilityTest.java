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

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilityTest {

    @Test
    void defaultsUseKnownCanonicalValues() {
        Capability capability = new Capability();

        assertEquals(Protocol.REST.value(), capability.getProtocol());
        assertEquals(Status.OPERATIONAL.value(), capability.getStatus());
    }

    @Test
    void setProtocolCanonicalizesKnownValue() {
        Capability capability = new Capability();

        capability.setProtocol(" grpc ");

        assertEquals(Protocol.GRPC.value(), capability.getProtocol());
    }

    @Test
    void setProtocolAcceptsUnknownValue() {
        Capability capability = new Capability();

        capability.setProtocol(" custom protocol ");

        assertEquals("CUSTOM PROTOCOL", capability.getProtocol());
    }

    @Test
    void setProtocolKeepsExplicitNull() {
        Capability capability = new Capability();

        capability.setProtocol((String) null);

        assertNull(capability.getProtocol());
    }

    @Test
    void setStatusCanonicalizesKnownValue() {
        Capability capability = new Capability();

        capability.setStatus(" maintenance ");

        assertEquals(Status.MAINTENANCE.value(), capability.getStatus());
    }

    @Test
    void setStatusAcceptsUnknownValue() {
        Capability capability = new Capability();

        capability.setStatus(" degraded ");

        assertEquals("DEGRADED", capability.getStatus());
    }

    @Test
    void setStatusKeepsExplicitNull() {
        Capability capability = new Capability();

        capability.setStatus((String) null);

        assertNull(capability.getStatus());
    }

    @Test
    void setEndpointAcceptsStringAsUriConvenience() {
        Capability capability = new Capability();

        capability.setEndpoint("https://node.eosc-beyond.eu/api/test");

        assertEquals(URI.create("https://node.eosc-beyond.eu/api/test"), capability.getEndpoint());
    }

    @Test
    void protocolFromValueMatchesKnownVariants() {
        assertEquals(Protocol.GRPC, Protocol.fromValue("gRPC").orElseThrow());
        assertEquals(Protocol.WEB_SOCKET, Protocol.fromValue("web socket").orElseThrow());
        assertEquals(Protocol.RSOCKET, Protocol.fromValue("r-socket").orElseThrow());
    }

    @Test
    void statusFromValueReturnsEmptyForUnknownValue() {
        assertTrue(Status.fromValue("degraded").isEmpty());
    }
}
