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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


//tag::annotateManagedExecutor[]
@ManagedExecutorDefinition(
    name = "java:module/concurrent/managed-executor")
//end::annotateManagedExecutor[]
@ApplicationScoped
public class SystemProperties {

    private static Logger logger = Logger.getLogger(SystemProperties.class.getName());

    // tag::managedExecutorService[]
    @Inject
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
        Map<String, Future<String>> properties = new HashMap<String, Future<String>>();
        // end::properties[]
        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith(prefix + "."))
                                  .collect(Collectors.toList());
        for (String k : keys) {
            // tag::submit[]
            Future<String> v = managedExecutor.submit(() -> {
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

}
