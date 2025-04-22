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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

public class SystemEndpointIT {

    private static final String PORT = System.getProperty("http.port");
    private static final String URL = "http://localhost:" + PORT + "/api/system";

    private static Client client;
    private static CountDownLatch countDown;
    
    @BeforeAll
    public static void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void teardown() {
        client.close();
    }

    @Test
    public void testGetProperties() {
        WebTarget target = client.target(URL + "/properties/os");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        JsonObject properties = response.readEntity(JsonObject.class);
        assertEquals(System.getProperty("os.name"), properties.getString("os.name"));
        response.close();
    }

    @Test
    public void testGetSystemLoad() throws Exception {
        startCountDown(3);
        URI uri = new URI("ws://localhost:9080/systemLoad");
        SystemClient client1 = new SystemClient(uri);
        SystemClient client2 = new SystemClient(uri);
        SystemClient client3 = new SystemClient(uri);
        WebTarget target = client.target(URL + "/systemLoad/5");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        countDown.await(10, TimeUnit.SECONDS);
        client1.close();
        client2.close();
        client3.close();
        assertEquals(0, countDown.getCount(),
                "The countDown was not 0.");
    }

    @Test
    public void testEnableSchedule() throws Exception {
        WebTarget target = client.target(URL + "/schedule");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        String r = response.readEntity(String.class);
        boolean scheduleWasEnabled = false;
        if (r.startsWith("Disabling")) {
            Thread.sleep(11000);
            response = target.request().get();
            assertEquals(200, response.getStatus(),
                "Incorrect response code from " + target.getUri().getPath());
            scheduleWasEnabled = true;
        }
        startCountDown(3);
        URI uri = new URI("ws://localhost:9080/systemLoad");
        SystemClient client1 = new SystemClient(uri);
        SystemClient client2 = new SystemClient(uri);
        SystemClient client3 = new SystemClient(uri);
        countDown.await(10, TimeUnit.SECONDS);
        client1.close();
        client2.close();
        client3.close();
        assertEquals(0, countDown.getCount(),
                    "The countDown was not 0.");
        if (!scheduleWasEnabled) {
            target.request().get();
        }
    }

    private static void startCountDown(int count) {
        countDown = new CountDownLatch(count);
    }

    public static void verify(JsonObject systemLoad) {
        assertNotNull(systemLoad.getString("time"));
        assertTrue(
            systemLoad.getJsonNumber("cpuLoad") != null
            || systemLoad.getJsonNumber("memoryUsage") != null
        );
        countDown.countDown();
    }

}
