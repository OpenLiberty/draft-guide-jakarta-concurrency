// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

public class SystemEndpointIT {

    private static final String PORT = System.getProperty("http.port");
    private static final String URL = "http://localhost:" + PORT + "/api/system";
    private static final MemoryMXBean MEM_BEAN = ManagementFactory.getMemoryMXBean();

    private static Client client;

    @BeforeAll
    public static void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void teardown() {
        client.close();
    }

    @Test
    public void testGetProperty() {
        WebTarget target = client.target(URL + "/property/os.name");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                "Incorrect response code from " + target.getUri().getPath());
        String osName = response.readEntity(String.class);
        assertEquals(System.getProperty("os.name"), osName);
        response.close();
    }

    @Test
    public void testGetHeapSize() {
        WebTarget target = client.target(URL + "/heapSize");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                "Incorrect response code from " + target.getUri().getPath());
        Long heapSize = response.readEntity(Long.class);
        assertEquals(MEM_BEAN.getHeapMemoryUsage().getMax(), heapSize);
        response.close();
    }

    @Test
    public void testGetMemoryUsed() {
        WebTarget target = client.target(URL + "/memoryUsed");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                "Incorrect response code from " + target.getUri().getPath());
        Long memoryUsed = response.readEntity(Long.class);
        assertTrue(memoryUsed > 0);
        response.close();
    }

    @Test
    public void testGetSystemLoad() {
        WebTarget target = client.target(URL + "/systemLoad");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
                "Incorrect response code from " + target.getUri().getPath());
        Double systemLoad = response.readEntity(Double.class);
        assertTrue(systemLoad >= 0.0);
        response.close();
    }
}
