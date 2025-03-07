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
package it.io.openliberty.guides.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.openliberty.guides.inventory.models.SystemData;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.UriBuilder;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryEndpointIT {

    private static final String PORT = System.getProperty("http.port");
    private static final String URL = "http://localhost:" + PORT + "/api";

    private static String hostname;

    private static InventoryResourceClient client;

    @BeforeAll
    public static void setup() throws Exception {
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        ResteasyClient resteasyClient = (ResteasyClient) builder.build();
        ResteasyWebTarget target = resteasyClient.target(UriBuilder.fromPath(URL));
        client = target.proxy(InventoryResourceClient.class);
        hostname = InetAddress.getLocalHost().getHostName();
    }

    private void assertSystem(String hostname,
        Boolean isMemoryUsageZero, Boolean isSystemLoadZero) {

        SystemData s = client.getSystem(hostname);
        assertEquals(hostname, s.getHostname());
        if (isMemoryUsageZero != null) {
            if (isMemoryUsageZero) {
                assertEquals(0.0, s.getMemoryUsage(),
                    hostname + " s.getMemoryUsage() = " + s.getMemoryUsage());
            } else {
                assertTrue(s.getMemoryUsage() > 0.0,
                    hostname + " s.getMemoryUsage() = " + s.getMemoryUsage());
            }
        }
        if (isSystemLoadZero != null) {
            if (isSystemLoadZero) {
                assertEquals(0.0, s.getSystemLoad(),
                    hostname + " s.getSystemLoad() = " + s.getSystemLoad());
            } else {
                assertTrue(s.getSystemLoad() > 0.0,
                    hostname + " s.getSystemLoad() = " + s.getSystemLoad());
            }
        }

    }

    // tag::testAddSystems[]
    @Test
    @Order(1)
    public void testAddSystems() throws Exception {
        client.addSystemClient("localhost");
        client.addSystemClient("127.0.0.1");
        client.addSystemClient(hostname);
        assertEquals(3, client.listContents().size());
        assertSystem("localhost", null, null);
        assertSystem("127.0.0.1", null, null);
        assertSystem(hostname, null, null);
    }
    // end::testAddSystems[]

    // tag::testUpdateMemoryUsed[]
    @Test
    @Order(2)
    public void testUpdateMemoryUsed() throws Exception {
        client.updateMemoryUsed(3);
        Thread.sleep(5000);
        assertSystem("localhost", false, null);
        assertSystem("127.0.0.1", false, null);
        assertSystem(hostname, false, null);
    }
    // end::testUpdateMemoryUsed[]

    // tag::testUpdateSystemLoad[]
    @Test
    @Order(3)
    public void testUpdateSystemLoad() throws Exception {
        client.updateSystemLoad(3);
        assertSystem("localhost", null, false);
        assertSystem("127.0.0.1", null, false);
        assertSystem(hostname, null, false);
    }
    // end::testUpdateSystemLoad[]

    // tag::testResetSystems[]
    @Test
    @Order(4)
    public void testResetSystems() throws Exception {
        client.resetSystems();
        assertSystem("localhost", true, true);
        assertSystem("127.0.0.1", true, true);
        assertSystem(hostname, true, true);
    }
    // end::testResetSystems[]

    // tag::testRemoveSystem[]
    @Test
    @Order(5)
    public void testRemoveSystem() throws Exception {
        client.removeSystem("127.0.0.1");
        assertEquals(2, client.listContents().size());
        assertSystem("localhost", true, true);
        assertNull(client.getSystem("127.0.0.1"));
        assertSystem(hostname, true, true);
    }
    // end::testRemoveSystem[]
}
