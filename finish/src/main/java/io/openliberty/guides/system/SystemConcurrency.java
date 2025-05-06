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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.naming.InitialContext;

import com.sun.management.OperatingSystemMXBean;

import io.openliberty.guides.system.model.SystemLoadData;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.transaction.UserTransaction;

// tag::annotateManagedScheduledExecutor[]
@ManagedScheduledExecutorDefinition(
    name = "java:module/concurrent/virtual-executor",
    qualifiers = WithVirtualThreads.class)
// end::annotateManagedScheduledExecutor[]
@ApplicationScoped
public class SystemConcurrency {

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

    // tag::entityManager[]
    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;
    // end::entityManager[]

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

    // tag::getCpuLoad[]
    public void getCpuLoad() {
        logger.info("New CPU load will be recorded after 5 seconds.");
        // tag::scheduleCall[]
        virtualManagedExecutor.schedule(() -> {
        // end::scheduleCall[]
            try {
                // tag::userTransaction[]
	            UserTransaction ut = (UserTransaction)
	                new InitialContext().lookup("java:comp/UserTransaction");
                // end::userTransaction[]
                // tag::utBegin[]
	            ut.begin();
                // end::utBegin[]
	            // tag::calculateCPULoad[]
	            SystemLoadData cpuLoadData  = new SystemLoadData();
	            LocalDateTime current = LocalDateTime.now();
	            cpuLoadData.setTime(current);
	            Double cpuLoad = Double.valueOf(OS.getCpuLoad() * 100.0);
	            cpuLoadData.setCpuLoad(cpuLoad);
	            // end::calculateCPULoad[]
                // tag::persistCPULoad[]
	            em.persist(cpuLoadData);
                // end::persistCPULoad[]
                // tag::utCommit[]
	            ut.commit();
                // end::utCommit[]
	            logger.info("CPU load at \"" + current + "\" was recorded.");
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        // tag::after[]
        }, 5, TimeUnit.SECONDS);
        // end::after[]
    }
    // end::getCpuLoad[]

    // tag::asynchronous1[]
    @Asynchronous
    // end::asynchronous1[]
    // tag::transactional[]
    @Transactional(value = TxType.REQUIRES_NEW)
    // end::transactional[]
    // tag::getMemoryUsage[]
    public void getMemoryUsage() {
        logger.info("New memory usage will be recorded after 5 seconds.");
        doSomething(5);
        // tag::calculateMemoryUsage[]
        SystemLoadData memoryUsageData  = new SystemLoadData();
        LocalDateTime current = LocalDateTime.now();
        memoryUsageData.setTime(current);
        long heapMax = MEM.getHeapMemoryUsage().getMax();
        long heapUsed = MEM.getHeapMemoryUsage().getUsed();
        Double memoryUsage = Double.valueOf(heapUsed * 100.0 / heapMax);
        memoryUsageData.setMemoryUsage(memoryUsage);
        // end::calculateMemoryUsage[]
        // tag::persistMemoryUsage[]
        em.persist(memoryUsageData);
        // end::persistMemoryUsage[]
        logger.info("Memory usage at \"" + current + "\" was recorded.");
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
    @Transactional(value = TxType.REQUIRES_NEW)
    public CompletableFuture<String> schedule() {
    // end::completableFuture[]
        if (isScheduleEnabled()) {
            // tag::callCalculateSystemLoad3[]
            SystemLoadData slData  = new SystemLoadData();
            LocalDateTime current = LocalDateTime.now();
            slData.setTime(current);
            Double cpuLoad = Double.valueOf(OS.getCpuLoad() * 100.0);
            slData.setCpuLoad(cpuLoad);
            long heapMax = MEM.getHeapMemoryUsage().getMax();
            long heapUsed = MEM.getHeapMemoryUsage().getUsed();
            Double memoryUsage = Double.valueOf(heapUsed * 100.0 / heapMax);
            slData.setMemoryUsage(memoryUsage);
            em.persist(slData);
            // end::callCalculateSystemLoad3[]
            logger.info("System load at \"" + current + " was recorded.");
            // tag::returnNull[]
            return null;
            // end::returnNull[]
        } else {
            logger.info("Schedule was disabled.");
            // tag::returnCompletableFuture[]
            return Asynchronous.Result.complete("Completed");
            // end::returnCompletableFuture[]
        }
    }
    // end::schedule[]

	public List<SystemLoadData> getSystemLoads() {
	    return em.createNamedQuery(
	           "SystemLoadData.findAll", SystemLoadData.class).getResultList();
	}

}
