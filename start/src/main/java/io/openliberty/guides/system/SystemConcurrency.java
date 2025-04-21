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
import java.text.SimpleDateFormat;
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

@ManagedScheduledExecutorDefinition(name = "java:module/concurrent/virtual-executor",
                           qualifiers = WithVirtualThreads.class,
                           virtual = true)
@ApplicationScoped
public class SystemConcurrency {

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEM =
        ManagementFactory.getMemoryMXBean();
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("HH:mm:ss");
    private static final Random RANDOM = new Random();

    private static Logger logger = Logger.getLogger(SystemConnency.class.getName());
    private static boolean schedulerEnabled = false;

    @Inject
    @WithVirtualThreads
    ManagedScheduledExecutorService virtualManagedExecutor;

    @Inject
    SystemLoadService sessions;

    public static boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public static void setSchedulerEnabled(boolean enabled) {
        schedulerEnabled = enabled;
    }

    public Map<String, String> getProperties(String prefix)
           throws InterruptedException, ExecutionException {

        Map<String, Future<String>> properties = new HashMap<String, Future<String>>();
        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith(prefix + "."))
                                  .collect(Collectors.toList());
        for (String k : keys) {
            properties.put(k, virtualManagedExecutor.submit(() -> {
                // get system property task
                logger.info("Getting the " + k + " property...");
                doSomething();          
                return System.getProperty(k);
            }));
        }
        return properties.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> {
                try {
                    Future<String> propertyValue = e.getValue();
                    String v = propertyValue.get();
                    logger.info("The value of the " + e.getKey() + " property: " + v);
                    return v;
                 } catch (Exception ex) {
                       return null;
                 }
            }));
    }

    @Asynchronous
    public void refresh(int after) {
        logger.info("New system load will be boardcast after " + after + " seconds.");
        virtualManagedExecutor.schedule(() -> {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("time", Calendar.getInstance().getTime().toString());
            builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
            long heapMax = MEM.getHeapMemoryUsage().getMax();
            long heapUsed = MEM.getHeapMemoryUsage().getUsed();
            builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
            JsonObject systemLoad = builder.build();
            sessions.sendToAllSessions(systemLoad);
            logger.info("New system load was boardcast");
       }, after, TimeUnit.SECONDS);
    }

    @Asynchronous(runAt = { @Schedule(cron = "*/10 * * * * *")}) 
    public CompletableFuture<Boolean> schedule() {
        if (!schedulerEnabled) {
            logger.info("Schedule was completed");
            return Asynchronous.Result.complete(Boolean.TRUE);
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        Date currentTime = Calendar.getInstance().getTime();
        builder.add("time", currentTime.toString());
        builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
        long heapMax = MEM.getHeapMemoryUsage().getMax();
        long heapUsed = MEM.getHeapMemoryUsage().getUsed();
        builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
        JsonObject systemLoad = builder.build();
        sessions.sendToAllSessions(systemLoad);
        logger.info("New system load at " + TIME_FORMAT.format(currentTime) + " was boardcast");
        return null;
    }

    private void doSomething() {
        try {
            Thread.sleep(RANDOM.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
