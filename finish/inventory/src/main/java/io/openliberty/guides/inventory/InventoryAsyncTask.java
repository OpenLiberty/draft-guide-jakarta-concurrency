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

import io.openliberty.guides.inventory.model.SystemData;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

@ApplicationScoped
public class InventoryAsyncTask {

    private static final MemoryMXBean MEM_BEAN =
            ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean OS_BEAN =
            ManagementFactory.getOperatingSystemMXBean();

    public SystemData createSystemData(String hostname) {
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        Long heapSize = MEM_BEAN.getHeapMemoryUsage().getMax();
        Long memoryUsage = MEM_BEAN.getHeapMemoryUsage().getUsed();
        Double systemLoad = OS_BEAN.getSystemLoadAverage();

        SystemData systemData = new SystemData();
        systemData.setHostname(hostname);
        systemData.setOsName(osName);
        systemData.setJavaVersion(javaVersion);
        systemData.setHeapSize(heapSize);
        systemData.setMemoryUsage(memoryUsage);
        systemData.setSystemLoad(systemLoad);

        return systemData;
    }
}
