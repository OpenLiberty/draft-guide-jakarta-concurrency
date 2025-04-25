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
import java.util.Random;
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
    virtual = true)
//end::annotateManagedScheduledExecutor[]
@ApplicationScoped
public class SystemConcurrency {

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEM = ManagementFactory.getMemoryMXBean();
    private static final Random RANDOM = new Random();

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
            Thread.sleep(RANDOM.nextInt(t * 1000));
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
            properties.put(k, virtualManagedExecutor.submit(() -> {
                return getSystemPropertyTask(k);
            }));
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
    private JsonObject calculateSystemLoad(boolean schedule) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (schedule) {
            builder.add("schedule", true);
        }
        Date current = Calendar.getInstance().getTime();
        builder.add("time", current.toString());
        builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
        long heapMax = MEM.getHeapMemoryUsage().getMax();
        long heapUsed = MEM.getHeapMemoryUsage().getUsed();
        builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
        return builder.build();
    }
    // end::calculateSystemLoad[]

    // tag::getSystemLoad[]
    @Asynchronous
    public void getSystemLoad(int after) {
        logger.info("New system load will be boardcast after " + after + " seconds.");
        virtualManagedExecutor.schedule(() -> {
            JsonObject systemLoad = calculateSystemLoad(false);
            sseSrvice.broadcast(systemLoad);
            logger.info("System load at \"" + systemLoad.getString("time")
                + "\" was boardcast.");
        }, after, TimeUnit.SECONDS);
    }
    // end::getSystemLoad[]

    // tag::enableSchedule[]
    public boolean isScheduleEnabled() {
        return scheduleEnabled;
    }

    public void enableSchedule(boolean enabled) {
        scheduleEnabled = enabled;
    }
    // end::enableSchedule[]

    // tag::schedule[]
    @Asynchronous(runAt = { @Schedule(cron = "*/10 * * * * *")})
    public CompletableFuture<String> schedule() {
        if (isScheduleEnabled()) {
            JsonObject systemLoad = calculateSystemLoad(true);
            sseSrvice.broadcast(systemLoad);
            logger.info("System load at \"" + systemLoad.getString("time")
                + " was boardcast.");
            return null;
        } else {
            logger.info("Schedule was disabled.");
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("schedule", false);
            JsonObject systemLoad = builder.build();
            sseSrvice.broadcast(systemLoad);
            return Asynchronous.Result.complete("Completed");
        }
    }
    // end::schedule[]

}
