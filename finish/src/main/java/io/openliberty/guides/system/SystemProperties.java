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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;


//tag::annotateManagedExecutor[]
@ManagedExecutorDefinition(
    name = "java:module/concurrent/managed-executor")
//end::annotateManagedExecutor[]
@ApplicationScoped
public class SystemProperties {

    private static Logger logger = Logger.getLogger(SystemProperties.class.getName());

    // tag::managedExecutorService[]
    @Resource(lookup = "java:module/concurrent/managed-executor")
    // tag::managedExecutor[]
    ManagedExecutorService managedExecutor;
    // end::managedExecutor[]
    // end::managedExecutorService[]

    private String getSystemPropertyTask(String key) throws InterruptedException {
        logger.info("Getting the " + key + " property...");
        Thread.sleep(1000);
        return System.getProperty(key);
    }

    public Map<String, String> getProperties(String prefix)
           throws InterruptedException, ExecutionException {

        // tag::properties[]
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
        // end::properties[]

        // tag::tasks[]
        List<Callable<String>> tasks = new ArrayList<>();
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith(prefix + ".")) {
                tasks.add(() -> {
                    // tag::getSystemPropertyTask[]
                    return properties.put(key, getSystemPropertyTask(key));
                    // end::getSystemPropertyTask[]
                });
            }
        }
        // end::tasks[]

        // tag::invokeAll[]
        managedExecutor.invokeAll(tasks);
        // end::invokeAll[]

        return properties;
    }

}
