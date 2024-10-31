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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUriExceptionMapper;
import io.openliberty.guides.inventory.models.SystemData;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InventoryAsyncTask {

    private static Logger logger = Logger.getLogger(InventoryAsyncTask.class.getName());

    @Inject
    @ConfigProperty(name = "client.http.port")
    String CLIENT_PORT;

    // tag::managedExecutor[]
    @Resource
    ManagedScheduledExecutorService managedExecutor;
    // end::managedExecutor[]

    // tag::getClientData[]
    public SystemData getClientData(String hostname) {
        try {
            // tag::submit1[]
            Future<String> osNameFuture = managedExecutor.submit(
            // end::submit1[]
            // tag::submitTask1[]
                () -> {
                    // tag::getSystemClient1[]
                    SystemClient client = getSystemClient(hostname);
                    // end::getSystemClient1[]
                    // tag::osName[]
                    String osName = client.getProperty("os.name");
                    // end::osName[]
                    client.close();
                    logger.info("Got OS name from " + hostname + ": " + osName);
                    return osName;
                });
            // end::submitTask1[]
            // tag::submit2[]
            Future<String> javaVerFuture = managedExecutor.submit(
            // end::submit2[]
                // tag::submitTask2[]
                () -> {
                    // tag::getSystemClient2[]
                    SystemClient client = getSystemClient(hostname);
                    // end::getSystemClient2[]
                    // tag::javaVer[]
                    String javaVer = client.getProperty("java.version");
                    // end::javaVer[]
                    client.close();
                    logger.info("Got Java version from " + hostname + ": " + javaVer);
                    return javaVer;
                });
                // end::submitTask2[]
            // tag::submit3[]
            Future<Long> heapSizeFuture = managedExecutor.submit(
            // end::submit3[]
                // tag::submitTask3[]
                () -> {
                    // tag::getSystemClient3[]
                    SystemClient client = getSystemClient(hostname);
                    // end::getSystemClient3[]
                    // tag::heapSize[]
                    Long heapSize = client.getHeapSize();
                    // end::heapSize[]
                    client.close();
                    logger.info("Got heap size from " + hostname + ": "  + heapSize);
                    return heapSize;
                });
                // end::submitTask3[]
            // tag::get[]
            return new SystemData(hostname,
                        osNameFuture.get(),
                        javaVerFuture.get(),
                        heapSizeFuture.get());
            // end::get[]
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // end::getClientData[]
    // tag::updateSystemsUsage[]
    // @Asynchronous(runAt = { @Schedule(cron = "*/15 * * * * *")})
    public void updateSystemsUsage(List<SystemData> systems, int after) {
        for (SystemData s : systems) {
            String hostname = s.getHostname();
            logger.info("Updating " + hostname + "...");
            managedExecutor.schedule(() -> {
            SystemClient client = null;
                try {
                    client = getSystemClient(hostname);
                    Long memoryUsed = client.getMemoryUsed();
                    Double systemLoad = client.getSystemLoad();
                    s.setMemoryUsed(memoryUsed);
                    s.setSystemLoad(systemLoad);
                    logger.info(hostname + " => memoryUsed: " + memoryUsed + ", "
                                + "systemLoad: " + systemLoad);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeClient(client);
                }
            }, after, TimeUnit.SECONDS);
        }
    }
    // end::updateSystemsUsage[]

    // tag::asynchronous1[]
    @Asynchronous
    // tag::updateSystemsMemoryUsed[]
    // tag::parameters[]
    public void updateSystemsMemoryUsed(List<SystemData> systems, int after) {
    // end::parameters[]
    // end::asynchronous1[]
    // tag::systems[]
    for (SystemData s : systems) {
    // end::systems[]
            // tag::getHostname[]
            String hostname = s.getHostname();
            // end::getHostname[]
            logger.info("Updating " + hostname + " memory usage...");
            // tag::schedule[]
            managedExecutor.schedule(() -> {
            // end::schedule[]
                SystemClient client = null;
                try {
                    // tag::getSystemClient4[]
                    client = getSystemClient(hostname);
                    // end::getSystemClient4[]
                    // tag::getMemoryUsed[]
                    Long memoryUsed = client.getMemoryUsed();
                    // end::getMemoryUsed[]
                    // tag::setMemoryUsage[]
                    s.setMemoryUsed(memoryUsed);
                    // end::setMemoryUsage[]
                    logger.info(hostname + " memory usage = " + s.getMemoryUsage());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeClient(client);
                }
            // tag::after[]
            }, after, TimeUnit.SECONDS);
            // end::after[]
        }
    }
    // end::updateSystemsMemoryUsed[]

    // tag::asynchronous2[]
    @Asynchronous()
    // tag::getSystemLoad[]
    // tag::getSystemLoadSignature[]
    public CompletableFuture<Double> getSystemLoad(String hostname, int after) {
    // end::getSystemLoadSignature[]
    // end::asynchronous2[]
    logger.info("Getting " + hostname + " recent system load...");
        Double systemLoad = null;
        SystemClient client = null;
        try {
            Thread.sleep(after * 1000);
            // tag::getSystemClient5[]
            client = getSystemClient(hostname);
            // end::getSystemClient5[]
            // tag::clientGetSystemLoad[]
            systemLoad = client.getSystemLoad();
            // end::clientGetSystemLoad[]
            logger.info(hostname + " recent system load = " + systemLoad);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeClient(client);
        }
        // tag::return[]
        return Asynchronous.Result.complete(systemLoad);
        // end::return[]
    }
    // end::getSystemLoad[]

    // tag::getSystemClientMethod[]
    private SystemClient getSystemClient(String hostname) throws Exception {
        String customURIString = "http://" + hostname + ":" + CLIENT_PORT + "/api";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                                .baseUri(customURI)
                                .register(UnknownUriExceptionMapper.class)
                                .build(SystemClient.class);
    }
    // end::getSystemClientMethod[]

    private void closeClient(SystemClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
