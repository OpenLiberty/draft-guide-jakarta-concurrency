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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemEndpointIT {

    private static final String PORT = System.getProperty("http.port");
    private static final String URL = "http://localhost:" + PORT + "/api/system";

    private static boolean isScheduleEnabled = false;

    private Client client;

    private static void checkSchedule() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL + "/schedule");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        String r = response.readEntity(String.class);
        isScheduleEnabled = r.equalsIgnoreCase("true");
        client.close();
    }

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        checkSchedule();
        if (isScheduleEnabled) {
            toggleSchedule();
        }
    }

    @BeforeEach
    public void beforeEach() {
      client = ClientBuilder.newClient();
    }

    @AfterEach
    public void afterEach() {
      client.close();
    }

    @AfterAll
    public static void afterAll() throws InterruptedException {
        if (!isScheduleEnabled) {
            toggleSchedule();
        }
    }

    private static void toggleSchedule() throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL + "/schedule/toggle");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        client.close();
        Thread.sleep(11000);
    }

    // tag::testGetProperties[]
    @Test
    @Order(1)
    public void testGetProperties() {
        WebTarget target = client.target(URL + "/properties/os");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        JsonObject properties = response.readEntity(JsonObject.class);
        assertEquals(4, properties.size());
        assertEquals(System.getProperty("os.name"), properties.getString("os.name"));
        response.close();
    }
    // end::testGetProperties[]

    private JsonArray getSystemLoads() {
        WebTarget target = client.target(URL + "/systemLoad");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        return response.readEntity(JsonArray.class);
    }

    private List<JsonObject> removeAll(JsonArray before, JsonArray after) {
        List<JsonObject> diff = new ArrayList<JsonObject>();
        after.forEach(o -> {
            if (!before.contains(o)) {
                diff.add((JsonObject) o);
            }
        });
        return diff;
    }

    private Map<String, JsonValue> testEndpoint(String endpoint) throws Exception {
        JsonArray before = getSystemLoads();
        WebTarget target = client.target(URL + endpoint);
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        Thread.sleep(6000);
        JsonArray after = getSystemLoads();
        assertEquals(before.size() + 1, after.size());
        List<JsonObject>  diff = removeAll(before, after);
        assertEquals(1, diff.size());
        return diff.get(0);
    }

    // tag::testGetCpuLoad[]
    @Test
    @Order(2)
    public void testGetCpuLoad() throws Exception {
        Map<String, JsonValue> systemLoad = testEndpoint("/systemLoad/cpuLoad");
        assertNotNull(systemLoad.get("cpuLoad"));
        assertNull(systemLoad.get("memoryUsage"));
    }
    // end::testGetCpuLoad[]

    // tag::testGetMemoryUsage[]
    @Test
    @Order(3)
    public void testGetMemoryUsage() throws Exception {
        Map<String, JsonValue> systemLoad = testEndpoint("/systemLoad/memoryUsage");
        assertNotNull(systemLoad.get("memoryUsage"));
        assertNull(systemLoad.get("cpuLoad"));
    }
    // end::testGetMemoryUsage[]

    // tag::testStartSchedule[]
    @Test
    @Order(4)
    public void testStartSchedule() throws Exception {
        toggleSchedule();
        JsonArray before = getSystemLoads();
        Thread.sleep(20000);
        JsonArray after = getSystemLoads();
        assertEquals(before.size() + 2, after.size());
        List<JsonObject>  diff = removeAll(before, after);
        assertEquals(2, diff.size());
        Map<String, JsonValue> systemLoad = diff.get(0);
        assertNotNull(systemLoad.get("cpuLoad"));
        assertNotNull(systemLoad.get("memoryUsage"));
    }
    // end::testStartSchedule[]

}
