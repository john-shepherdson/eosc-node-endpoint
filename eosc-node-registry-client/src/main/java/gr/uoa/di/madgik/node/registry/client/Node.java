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
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.net.URI;
import java.util.List;

/**
 * Node Registry entry enriched with endpoint capabilities when available.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Node {

    private String id;
    private String name;
    private URI logo;
    private String pid;
    private LegalEntity legalEntity;
    private URI nodeEndpoint;
    private List<Capability> capabilities;

    public Node() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getLogo() {
        return logo;
    }

    public void setLogo(URI logo) {
        this.logo = logo;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public LegalEntity getLegalEntity() {
        return legalEntity;
    }

    public void setLegalEntity(LegalEntity legalEntity) {
        this.legalEntity = legalEntity;
    }

    public URI getNodeEndpoint() {
        return nodeEndpoint;
    }

    public void setNodeEndpoint(URI nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }
}
