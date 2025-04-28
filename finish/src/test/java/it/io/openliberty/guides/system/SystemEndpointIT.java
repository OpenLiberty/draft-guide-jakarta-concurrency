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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemEndpointIT {

    private static final String PORT = System.getProperty("http.port");
    private static final String URL = "http://localhost:" + PORT + "/api/system";
    private static final Jsonb JSONB = JsonbBuilder.create();

    private static boolean isScheduleEnabled = false;
    private static CountDownLatch countDown;

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

    private static void createSseClient() {
        CompletableFuture.runAsync(() -> {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(URL + "/sse");
            SseEventSource sse = SseEventSource.target(target).build();
            sse.register(new Consumer<InboundSseEvent>() {
                @Override
                public void accept(InboundSseEvent event) {
                    String data = event.readData();
                    JsonObject systemLoad = JSONB.fromJson(data, JsonObject.class);
                    if (data.contains("time")) {
                        assertTrue(
                            systemLoad.getJsonNumber("cpuLoad") != null
                            || systemLoad.getJsonNumber("memoryUsage") != null
                        );
                        countDown.countDown();
                    }
                }
            });
            sse.open();
            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sse.close();
            client.close();
        }).thenAccept(result -> {
            System.out.println("SSE client was closed.");
        });
    }

    private static void startCountDown(int count) {
        countDown = new CountDownLatch(count);
    }

    @BeforeAll
    public static void beforeAll() {
        checkSchedule();
        createSseClient();
    }

    @BeforeEach
    public void beforeEach() {
      client = ClientBuilder.newClient();
    }

    @AfterEach
    public void afterEach() {
      client.close();
    }

    private void toggleSchedule() throws InterruptedException {
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

    // tag::testGetSystemLoad[]
    @Test
    @Order(2)
    public void testGetSystemLoad() throws Exception {
        if (isScheduleEnabled) {
            toggleSchedule();
        }
        startCountDown(1);
        WebTarget target = client.target(URL + "/systemLoad/5");
        Response response = target.request().get();
        assertEquals(200, response.getStatus(),
            "Incorrect response code from " + target.getUri().getPath());
        countDown.await(10, TimeUnit.SECONDS);
        assertEquals(0, countDown.getCount(),
                "The countDown was not 0.");
    }
    // end::testGetSystemLoad[]

    // tag::testEnableSchedule[]
    @Test
    @Order(3)
    public void testEnableSchedule() throws Exception {
        toggleSchedule();
        startCountDown(3);
        countDown.await(35, TimeUnit.SECONDS);
        assertEquals(0, countDown.getCount(),
                    "The countDown was not 0.");
        if (!isScheduleEnabled) {
            toggleSchedule();
        }
    }
    // end::testEnableSchedule[]

}
