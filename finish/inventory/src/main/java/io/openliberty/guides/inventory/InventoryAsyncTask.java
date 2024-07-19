// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.inventory;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUriExceptionMapper;
import io.openliberty.guides.inventory.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

@ApplicationScoped
public class InventoryAsyncTask {
    private Client client = ClientBuilder.newClient();
    private static String port = System.getProperty("client.https.port");


    public String getProperty(String hostname) {
        WebTarget target = client.target("http://" + hostname + ":" + port + "/property/");
        Response response = target.request(MediaType.TEXT_PLAIN).get();
        try {
            if (response.getStatus() == 200) {
                return response.readEntity(String.class);
            } else {
                throw new WebApplicationException("Get Property Error: " + response.getStatus());
            }
        } finally {
            response.close();
        }
    }

    private SystemClient getSystemClient(String hostname) {
        String customURIString = "https://" + hostname + ":" + port + "/system";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                .baseUri(customURI)
                .register(UnknownUriExceptionMapper.class)
                .build(SystemClient.class);
    }

    public SystemData createSystemData(String hostname) {
        SystemClient systemClient = getSystemClient(hostname);
        String osName = systemClient.getProperty("osName");
        String javaVersion = systemClient.getProperty("javaVersion");
        Long heapSize = systemClient.getHeapSize();
        Long memoryUsed = systemClient.getMemoryUsed();
        Double systemLoad = systemClient.getSystemLoad();

        SystemData systemData = new SystemData();
        systemData.setHostname(hostname);
        systemData.setOsName(osName);
        systemData.setJavaVersion(javaVersion);
        systemData.setHeapSize(heapSize);
        systemData.setMemoryUsage(memoryUsed);
        systemData.setSystemLoad(systemLoad);

        return systemData;
    }
}
