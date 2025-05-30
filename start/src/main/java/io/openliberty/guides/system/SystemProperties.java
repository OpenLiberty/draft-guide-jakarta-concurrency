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
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemProperties {

    private static Logger logger = Logger.getLogger(SystemProperties.class.getName());

    // tag::getSystemPropertyTask[]
    private String getSystemPropertyTask(String key) throws InterruptedException {
        logger.info("Getting the " + key + " property...");
        Thread.sleep(1000);
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
