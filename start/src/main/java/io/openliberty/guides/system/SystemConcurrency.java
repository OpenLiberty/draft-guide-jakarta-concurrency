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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.management.OperatingSystemMXBean;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemConcurrency {

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEM = ManagementFactory.getMemoryMXBean();

    private static Logger logger = Logger.getLogger(SystemConcurrency.class.getName());

    private void doSomething(int t) {
        try {
            // tag::doSomethingSleep[]
            Thread.sleep(t * 1000);
            // end::doSomethingSleep[]
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        }
    }

    // tag::getSystemPropertyTask[]
    private String getSystemPropertyTask(String key) {
        logger.info("Getting the " + key + " property...");
        doSomething(1);
        return System.getProperty(key);
    }
    // end::getSystemPropertyTask[]

    // tag::getProperties[]
    public Map<String, String> getProperties(String prefix)
           throws InterruptedException, ExecutionException {

        Map<String, String> properties = new HashMap<String, String>();
        List<String> keys = System.getProperties().stringPropertyNames().stream()
                                  .filter(k -> k.startsWith(prefix + "."))
                                  .collect(Collectors.toList());
        for (String k : keys) {
            // tag::callGetSystemPropertyTask[]
            properties.put(k, getSystemPropertyTask(k));
            // end::callGetSystemPropertyTask[]
        }
        return properties;
    }
    // end::getProperties[]

}
