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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
public class SystemConnencyBean {
    
    private static Logger logger = Logger.getLogger(SystemConnencyBean.class.getName());

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEM =
        ManagementFactory.getMemoryMXBean();
    private static final Random RANDOM = new Random();
    
    @Inject
    @WithVirtualThreads
    ManagedScheduledExecutorService virtualManagedExecutor;

    @Inject
    SystemLoadService sessions;

    public Map<String, String> getProperties(String prefix) throws InterruptedException, ExecutionException {
        Map<String, Future<String>> osProperties = new HashMap<String, Future<String>>();
        List<String> osKeys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith(prefix + "."))
                                  .collect(Collectors.toList());
        for (String k : osKeys) {
            osProperties.put(k, virtualManagedExecutor.submit(() -> {
                // get system property task
                logger.info("Getting the " + k + " property");
                doSomething();          
                return System.getProperty(k);
            }));
        }
        return osProperties.entrySet().stream()
               .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                   try {
                       Future<String> propertyValue = entry.getValue();
                       String value = propertyValue.get();
                       logger.info("Got the " + entry.getKey() + " property value: " + value);
                       doSomething();          
                       return value;
                   } catch (Exception e) {
                       return null;
                   }
                }));
    }

    @Asynchronous
    public void refresh() {
        logger.info("Refresh the system load after 5 seconds");
        virtualManagedExecutor.schedule(() -> {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("time", Calendar.getInstance().getTime().toString());
            builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
            long heapMax = MEM.getHeapMemoryUsage().getMax();
            long heapUsed = MEM.getHeapMemoryUsage().getUsed();
            builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
            JsonObject systemLoad = builder.build();
            sessions.sendToAllSessions(systemLoad);
            logger.info("New system load was boardcasted");
       }, 5, TimeUnit.SECONDS);
    }

    @Asynchronous(runAt = { @Schedule(cron = "*/10 * * * * *")}) 
    public void schedule() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("time", Calendar.getInstance().getTime().toString());
        builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
        long heapMax = MEM.getHeapMemoryUsage().getMax();
        long heapUsed = MEM.getHeapMemoryUsage().getUsed();
        builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));
        JsonObject systemLoad = builder.build();
        sessions.sendToAllSessions(systemLoad);
        logger.info("New system load was boardcasted");
    }

    private void doSomething() {
        try {
            Thread.sleep(RANDOM.nextInt(1500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
