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

    // tag::testAddSystems[]
    @Test
    @Order(1)
    public void testAddSystems() throws Exception {

        client.addSystemClient("localhost");
        client.addSystemClient("127.0.0.1");
        client.addSystemClient(hostname);

        assertEquals(3, client.listContents().size());

        SystemData s = client.getSystem("localhost");
        assertEquals("localhost", s.getHostname());
        s = client.getSystem("127.0.0.1");
        assertEquals("127.0.0.1", s.getHostname());
        s = client.getSystem(hostname);
        assertEquals(hostname, s.getHostname());

    }
    // end::testAddSystems[]

    // tag::testUpdateMemoryUsed[]
    @Test
    @Order(2)
    public void testUpdateMemoryUsed() throws Exception {

        client.updateMemoryUsed(3);
        Thread.sleep(5000);

        SystemData s = client.getSystem("localhost");
        assertEquals("localhost", s.getHostname());
        assertTrue(s.getMemoryUsage() > 0.0);

        s = client.getSystem("127.0.0.1");
        assertEquals("127.0.0.1", s.getHostname());
        assertTrue(s.getMemoryUsage() > 0.0);

        s = client.getSystem(hostname);
        assertEquals(hostname, s.getHostname());
        assertTrue(s.getMemoryUsage() > 0.0);

    }
    // end::testUpdateMemoryUsed[]

    // tag::testUpdateSystemLoad[]
    @Test
    @Order(3)
    public void testUpdateSystemLoad() throws Exception {

        client.updateSystemLoad(3);

        SystemData s = client.getSystem("localhost");
        assertEquals("localhost", s.getHostname());
        assertTrue(s.getSystemLoad() > 0.0);

        s = client.getSystem("127.0.0.1");
        assertEquals("127.0.0.1", s.getHostname());
        assertTrue(s.getSystemLoad() > 0.0);

        s = client.getSystem(hostname);
        assertEquals(hostname, s.getHostname());
        assertTrue(s.getSystemLoad() > 0.0);

    }
    // end::testUpdateSystemLoad[]

    // tag::testResetSystems[]
    @Test
    @Order(4)
    public void testResetSystems() throws Exception {

        client.resetSystems();

        SystemData s = client.getSystem("localhost");
        assertEquals("localhost", s.getHostname());
        assertEquals(0.0, s.getMemoryUsage());
        assertEquals(0.0, s.getSystemLoad());

        s = client.getSystem("127.0.0.1");
        assertEquals("127.0.0.1", s.getHostname());
        assertEquals(0.0, s.getMemoryUsage());
        assertEquals(0.0, s.getSystemLoad());

        s = client.getSystem(hostname);
        assertEquals(hostname, s.getHostname());
        assertEquals(0.0, s.getMemoryUsage());
        assertEquals(0.0, s.getSystemLoad());

    }
    // end::testResetSystems[]

    // tag::testRemoveSystem[]
    @Test
    @Order(5)
    public void testRemoveSystem() throws Exception {

        client.removeSystem("127.0.0.1");

        assertEquals(2, client.listContents().size());

        SystemData s = client.getSystem("localhost");
        assertEquals("localhost", s.getHostname());
        assertEquals(0.0, s.getMemoryUsage());
        assertEquals(0.0, s.getSystemLoad());

        s = client.getSystem("127.0.0.1");
        assertNull(s);

        s = client.getSystem(hostname);
        assertEquals(hostname, s.getHostname());
        assertEquals(0.0, s.getMemoryUsage());
        assertEquals(0.0, s.getSystemLoad());

    }
    // end::testRemoveSystem[]
}
