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
package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Random;

import com.sun.management.OperatingSystemMXBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/system")
public class SystemResource {

    private static final MemoryMXBean MEM_BEAN = ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean OS_MEAN =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final Random RANDOM = new Random();

    @GET
    @Path("/property/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProperty(@PathParam("property") String property) {
        doSomething();
        return System.getProperty(property);
    }

    @GET
    @Path("/heapSize")
    @Produces(MediaType.TEXT_PLAIN)
    public Long getHeapSize() {
        doSomething();
        return MEM_BEAN.getHeapMemoryUsage().getMax();
    }
    
    @GET
    @Path("/memoryUsed")
    @Produces(MediaType.TEXT_PLAIN)
    public Long getMemoryUsed() {
        doSomething();
        return MEM_BEAN.getHeapMemoryUsage().getUsed();
    }

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.TEXT_PLAIN)
    public Double getSystemLoad() {
        doSomething();
        return OS_MEAN.getCpuLoad();
    }
    
    private void doSomething() {
        try {
            Thread.sleep(RANDOM.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
