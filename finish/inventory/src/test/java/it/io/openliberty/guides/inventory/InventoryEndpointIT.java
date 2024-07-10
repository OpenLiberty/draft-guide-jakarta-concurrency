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
// tag::testClass[]
package it.io.openliberty.guides.inventory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryEndpointIT {
    private static String port;
    private static String baseUrl;
    private static String hostname;
    private static String appPath = "inventory/api/";

    private static InventoryResourceClient client;
    private static String authHeader;

    @BeforeAll
    public static void setupTestClass() throws Exception {
        System.out.println("TEST: Starting Liberty Container setup");
        client = createRestClient(
                InventoryResourceClient.class, appPath);
        String userPassword = "bob" + ":" + "bobpwd";
        authHeader = "Basic "
                + Base64.getEncoder().encodeToString(userPassword.getBytes());
    }

    private void showSystemData(SystemData system) {
        System.out.println("TEST: SystemData > "
                + system.getId() + ", "
                + system.getHostname() + ", "
                + system.getOsName() + ", "
                + system.getJavaVersion() + ", "
                + system.getHeapSize());
    }

    @Test
    @Order(1)
    public void testAddSystem() {
        System.out.println("TEST: Testing add a system");
        client.addSystem("localhost", "linux", "17", Long.valueOf(2048));
        List<SystemData> systems = client.listContents();
        assertEquals(1, systems.size());
        showSystemData(systems.get(0));
        assertEquals("17", systems.get(0).getJavaVersion());
        assertEquals(Long.valueOf(2048), systems.get(0).getHeapSize());
    }

    @Test
    @Order(2)
    public void testUpdateSystem() {
        System.out.println("TEST: Testing update a system");
        client.updateSystem(authHeader, "localhost", "linux", "8", Long.valueOf(1024));
        SystemData system = client.getSystem("localhost");
        showSystemData(system);
        assertEquals("8", system.getJavaVersion());
        assertEquals(Long.valueOf(1024), system.getHeapSize());
    }

    @Test
    @Order(3)
    public void testRemoveSystem() {
        System.out.println("TEST: Testing remove a system");
        client.removeSystem(authHeader, "localhost");
        List<SystemData> systems = client.listContents();
        assertEquals(0, systems.size());
    }

    public static <T> T createRestClient(Class<T> clazz, String applicationPath) {
        String urlPath = getBaseURL();
        if (applicationPath != null) {
            urlPath += applicationPath;
        }
        ClientBuilder builder = ResteasyClientBuilder.newBuilder();
        ResteasyClient client = (ResteasyClient) builder.build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(urlPath));
        return target.proxy(clazz);
    }

    public static String getBaseURL() {
        port = System.getProperty("http.port");
        baseUrl = "http://localhost:" + port + "/";
        System.out.println("TEST: " + baseUrl);
        return baseUrl;
    }


}
