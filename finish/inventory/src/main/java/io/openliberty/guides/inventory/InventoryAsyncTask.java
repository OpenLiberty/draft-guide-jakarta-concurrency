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
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@ApplicationScoped
public class InventoryAsyncTask {

    @Resource
    private ManagedExecutorService managedExecutor;

    private Client client = ClientBuilder.newClient();

    private static String port = System.getProperty("client.https.port");

    private SystemClient getSystemClient(String hostname) {
        String customURIString = "https://" + hostname + ":" + port + "/system";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                .baseUri(customURI)
                .register(UnknownUriExceptionMapper.class)
                .build(SystemClient.class);
    }

    @GET
    @Path("parallelJob")
    @Produces(MediaType.TEXT_PLAIN)
    public SystemData createSystemData(String hostname) throws ExecutionException, InterruptedException {
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

        return systemData;
    }
}
