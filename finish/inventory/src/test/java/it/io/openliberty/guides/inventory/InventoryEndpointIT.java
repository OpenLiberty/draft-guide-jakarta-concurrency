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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryEndpointIT {

    private static final String PORT = System.getProperty("http.port");
    private static final String URL = "http://localhost:" + PORT + "/api";
    private static final Jsonb JSONB = JsonbBuilder.create();
    
    private static String hostname;
    
    private CloseableHttpClient client;

    @BeforeEach
    public void setup() {
        client = HttpClientBuilder.create().build();
    }

    @AfterEach
    public void teardown() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @BeforeAll
    public static void setupTestClass() throws Exception {
        hostname = InetAddress.getLocalHost().getHostName();
    }

    private void addSystem(String hostname) throws IOException {
        HttpPost httpPost = new HttpPost(URL + "/inventory/system/" + hostname);
        client.execute(httpPost, response -> { return response; });
    }
    
    private void putSystemsRequest(String request, int after) throws IOException {
        HttpPut httpPut = new HttpPut(URL + "/inventory/systems/"
                                      + request + "?after=" + after);
        client.execute(httpPut, response -> { 
            assertEquals(200, response.getCode());
            return response; });
    }
    
    private void deleteSystem(String hostname) throws IOException {
        HttpDelete httpDelete = new HttpDelete(URL + "/inventory/system/" + hostname);
        client.execute(httpDelete, response -> { 
            assertEquals(200, response.getCode());
            return response; });
    }

    private void assertSystem(String hostname, String property) throws IOException {
        HttpGet httpGet = new HttpGet(URL + "/inventory/system/" + hostname);
        client.execute(httpGet, response -> { 
            String responseText = EntityUtils.toString(response.getEntity());
            JsonObject system = JSONB.fromJson(responseText, JsonObject.class);
            assertTrue(system.getString("hostname").equals(hostname));
            String javaVersion = system.getString("javaVersion");
            assertTrue(javaVersion.contains("17") || javaVersion.contains("21"));
            assertTrue(system.getJsonNumber("heapSize").longValue() > 0);
            assertTrue(system.getJsonNumber(property).doubleValue() > 0.0);
            return response;
        });
    }
    
    private void assertSystems(int expectedSize) throws IOException {
        HttpGet httpGet = new HttpGet(URL + "/inventory/systems");
        client.execute(httpGet, response -> {
            assertEquals(200, response.getCode());
            String responseText = EntityUtils.toString(response.getEntity());
            JsonArray systems = JSONB.fromJson(responseText, JsonArray.class);
            assertEquals(expectedSize, systems.size());
            JsonObject system = systems.getJsonObject(0);
            String javaVersion = system.getString("javaVersion");
            assertTrue(javaVersion.contains("17") || javaVersion.contains("21"));
            assertTrue(system.getJsonNumber("heapSize").longValue() > 0);
            return response;
        });
    }

    @Test
    @Order(1)
    public void testAddSystems() throws Exception {
        addSystem("localhost");
        addSystem("127.0.0.1");
        addSystem(hostname);
        assertSystems(3);
    }

    @Test
    @Order(2)
    public void testUpdateMemoryUsed() throws Exception {
        putSystemsRequest("memoryUsed", 3);
        Thread.sleep(5000);
        assertSystem("localhost","memoryUsage");
    }

    @Test
    @Order(3)
    public void testUpdateSystemLoad() throws Exception {
        putSystemsRequest("systemLoad", 3);
        Thread.sleep(5000);
        assertSystem("localhost","systemLoad");
    }

    @Test
    @Order(5)
    public void testRemoveSystem() throws Exception {
        deleteSystem("127.0.0.1");
        assertSystems(2);
    }

}