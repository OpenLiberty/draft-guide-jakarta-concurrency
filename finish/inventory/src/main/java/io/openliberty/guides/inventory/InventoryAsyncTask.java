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

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUriExceptionMapper;
import io.openliberty.guides.inventory.models.SystemData;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InventoryAsyncTask {

    private static Logger logger = Logger.getLogger(InventoryAsyncTask.class.getName());

    @Inject
    @ConfigProperty(name = "client.http.port")
    String CLIENT_PORT;

    @Resource
    ManagedExecutorService managedExecutor;

    public SystemData getClientData(String hostname) {
        try {
            Future<String> osNameFuture = managedExecutor.submit(() -> {
                SystemClient client = getSystemClient(hostname);
                String osName = client.getProperty("os.name");
                client.close();
                logger.info("Got OS name from " + hostname + ": " + osName);
                return osName; });
            Future<String> javaVerFuture = managedExecutor.submit(() -> {
                SystemClient client = getSystemClient(hostname);
                String javaVer = client.getProperty("java.version");
                client.close();
                logger.info("Got Java version from " + hostname + ": " + javaVer);
                return javaVer; });
            Future<Long> heapSizeFuture = managedExecutor.submit(() -> {
                SystemClient client = getSystemClient(hostname);
                Long heapSize = client.getHeapSize();
                client.close();
                logger.info("Got heap size from " + hostname + ": "  + heapSize);
                return heapSize; });
            return new SystemData(hostname,
                        osNameFuture.get(),
                        javaVerFuture.get(),
                        heapSizeFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // @Asynchronous(runAt = { @Schedule(cron = "*/15 * * * * *")})
    public void updateSystemsUsage(List<SystemData> systems, int after) {
        for (SystemData s : systems) {
            String hostname = s.getHostname();
            logger.info("Updating " + hostname + "...");
            managedExecutor.submit(() -> {
                SystemClient client = null;
                try {
                    Thread.sleep(after);
                    client = getSystemClient(hostname);
                    Long memoryUsed = client.getMemoryUsed();
                    Double systemLoad = client.getSystemLoad();
                    s.setMemoryUsage(memoryUsed);
                    s.setSystemLoad(systemLoad);
                    logger.info(hostname + " => memoryUsed: " + memoryUsed + ", "
                                + "systemLoad: " + systemLoad);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (client != null) {
                        try {
                            client.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Asynchronous
    public void updateSystemsMemoryUsed(List<SystemData> systems, int after) {
        for (SystemData s : systems) {
            String hostname = s.getHostname();
            logger.info("Updating " + hostname + " memory usage...");
            managedExecutor.submit(() -> {
                SystemClient client = null;
                try {
                    Thread.sleep(after);
                    client = getSystemClient(hostname);
                    Long memoryUsed = client.getMemoryUsed();
                    s.setMemoryUsage(memoryUsed);
                    logger.info(hostname + " memory usage = " + s.getMemoryUsage());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (client != null) {
                        try {
                            client.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Asynchronous()
    public CompletableFuture<Double> getSystemLoad(String hostname, int after) {
        logger.info("Getting " + hostname + " recent system load...");
        Double systemLoad = null;
        SystemClient client = null;
        try {
            Thread.sleep(after);
            client = getSystemClient(hostname);
            systemLoad = client.getSystemLoad();
            logger.info(hostname + " recent system load = " + systemLoad);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Asynchronous.Result.complete(systemLoad);
    }

    private SystemClient getSystemClient(String hostname) throws Exception {
        String customURIString = "http://" + hostname + ":" + CLIENT_PORT + "/api";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                                .baseUri(customURI)
                                .register(UnknownUriExceptionMapper.class)
                                .build(SystemClient.class);
    }

}
