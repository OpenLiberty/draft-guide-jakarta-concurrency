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
import java.lang.management.OperatingSystemMXBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/")
public class SystemResource {

    private static final OperatingSystemMXBean OS_MEAN =
            ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEM_BEAN = ManagementFactory.getMemoryMXBean();

    @GET
    @Path("/property/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProperty(@PathParam("property") String property) {
        return System.getProperty(property);
    }

    @GET
    @Path("/heapsize")
    @Produces(MediaType.TEXT_PLAIN)
    public Long getHeapSize() {
        return MEM_BEAN.getHeapMemoryUsage().getMax();
    }

    @GET
    @Path("/memoryUsed")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getMemoryUsed() {
        return MEM_BEAN.getHeapMemoryUsage().getUsed();
    }

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public Double getSystemLoad() {
        return OS_MEAN.getSystemLoadAverage();
    }



}
