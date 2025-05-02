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
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.management.OperatingSystemMXBean;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

// tag::annotateManagedScheduledExecutor[]
@ManagedScheduledExecutorDefinition(
    name = "java:module/concurrent/virtual-executor",
    qualifiers = WithVirtualThreads.class,
// tag::virtual[]
    virtual = true)
// end::virtual[]
// end::annotateManagedScheduledExecutor[]
@ApplicationScoped
public class SystemConcurrency {

    private enum Option { CPU_LOAD, MEMORY_USAGE };

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEM = ManagementFactory.getMemoryMXBean();

    private static Logger logger = Logger.getLogger(SystemConcurrency.class.getName());
    private static boolean scheduleEnabled = false;

    // tag::managedScheduledExecutorService[]
    @Inject
    @WithVirtualThreads
    // tag::virtualManagedExecutor[]
    ManagedScheduledExecutorService virtualManagedExecutor;
    // end::virtualManagedExecutor[]
    // end::managedScheduledExecutorService[]

    @Inject
    SseService sseSrvice;

    private void doSomething(int t) {
        try {
            Thread.sleep(t * 1000);
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        }
    }

    private String getSystemPropertyTask(String key) {
        logger.info("Getting the " + key + " property...");
        doSomething(1);
        return System.getProperty(key);
    }

    public Map<String, String> getProperties(String prefix)
           throws InterruptedException, ExecutionException {

        // tag::properties[]
        Map<String, Future<String>> properties = new HashMap<String, Future<String>>();
        // end::properties[]
        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith(prefix + "."))
                                  .collect(Collectors.toList());
        for (String k : keys) {
            // tag::submit[]
            Future<String> v = virtualManagedExecutor.submit(() -> {
                return getSystemPropertyTask(k);
            });
            properties.put(k, v);
            // end::submit[]
        }
        // tag::collect[]
        return properties.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> {
                try {
                    // tag::get[]
                    Future<String> propertyValue = e.getValue();
                    String v = propertyValue.get();
                    // end::get[]
                    logger.info("The value of the " + e.getKey() + " property: " + v);
                    return v;
                 } catch (Exception ex) {
                       return null;
                 }
            }));
        // end::collect[]
    }

    // tag::subscribe[]
    public void subscribe(SseEventSink sink, Sse sse) {
        sseSrvice.subscribe(sink, sse);
    }
    // end::subscribe[]

    // tag::calculateSystemLoad[]
    private JsonObject calculateSystemLoad(boolean schedule, Option option) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (schedule) {
            builder.add("schedule", true);
        }
        Date current = Calendar.getInstance().getTime();
        builder.add("time", current.toString());
        if (option == Option.CPU_LOAD || option == null) {
            builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
        }
        if (option == Option.MEMORY_USAGE || option == null) {
            long heapMax = MEM.getHeapMemoryUsage().getMax();
            long heapUsed = MEM.getHeapMemoryUsage().getUsed();
            builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
        }
        return builder.build();
    }
    // end::calculateSystemLoad[]

    // tag::getCpuLoad[]
    public void getCpuLoad() {
        logger.info("New CPU load will be boardcast after 5 seconds.");
        // tag::scheduleCall[]
        virtualManagedExecutor.schedule(() -> {
        // end::scheduleCall[]
            // tag::callCalculateSystemLoad1[]
            JsonObject systemLoad = calculateSystemLoad(false, Option.CPU_LOAD);
            // end::callCalculateSystemLoad1[]
            // tag::broadcast1[]
            sseSrvice.broadcast(systemLoad);
            // end::broadcast1[]
            logger.info("CPU load at \"" + systemLoad.getString("time")
                + "\" was boardcast.");
        // tag::after[]
        }, 5, TimeUnit.SECONDS);
        // end::after[]
    }
    // end::getCpuLoad[]

    // tag::asynchronous1[]
    @Asynchronous
    // end::asynchronous1[]
    // tag::getMemoryUsage[]
    public void getMemoryUsage() {
        logger.info("New memory usage will be boardcast after 5 seconds.");
        doSomething(5);
        // tag::callCalculateSystemLoad2[]
        JsonObject systemLoad = calculateSystemLoad(false, Option.MEMORY_USAGE);
        // end::callCalculateSystemLoad2[]
        // tag::broadcast2[]
        sseSrvice.broadcast(systemLoad);
        // end::broadcast2[]
        logger.info("Memory usage at \"" + systemLoad.getString("time")
            + "\" was boardcast.");
    }
    // end::getMemoryUsage[]

    // tag::enableSchedule[]
    public boolean isScheduleEnabled() {
        return scheduleEnabled;
    }

    public void enableSchedule(boolean enabled) {
        scheduleEnabled = enabled;
    }
    // end::enableSchedule[]

    // tag::asynchronous2[]
    @Asynchronous(runAt = { @Schedule(cron = "*/10 * * * * *")})
    // end::asynchronous2[]
    // tag::schedule[]
    // tag::completableFuture[]
    public CompletableFuture<String> schedule() {
    // end::completableFuture[]
        if (isScheduleEnabled()) {
            // tag::callCalculateSystemLoad3[]
            JsonObject systemLoad = calculateSystemLoad(true, null);
            // end::callCalculateSystemLoad3[]
            // tag::broadcast3[]
            sseSrvice.broadcast(systemLoad);
            // end::broadcast3[]
            logger.info("System load at \"" + systemLoad.getString("time")
                + " was boardcast.");
            // tag::returnNull[]
            return null;
            // end::returnNull[]
        } else {
            logger.info("Schedule was disabled.");
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("schedule", false);
            JsonObject systemLoad = builder.build();
            sseSrvice.broadcast(systemLoad);
            // tag::returnCompletableFuture[]
            return Asynchronous.Result.complete("Completed");
            // end::returnCompletableFuture[]
        }
    }
    // end::schedule[]

}
