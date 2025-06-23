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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.openliberty.guides.system.model.SystemLoadData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/system")
public class SystemResource {

    @Inject
    SystemProperties propertiesBean;

    @Inject
    SystemConcurrency concurrencyBean;

    @GET
    @Path("/properties/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getProperties(@PathParam("prefix") String prefix)
        throws InterruptedException, ExecutionException {
        return propertiesBean.getProperties(prefix);
    }

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SystemLoadData> getSystemLoads() {
        return concurrencyBean.getSystemLoads();
    }

    // tag::getCpuLoad[]
    @GET
    @Path("/systemLoad/cpuLoad")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCpuLoad() {
        concurrencyBean.getCpuLoad();
        return "Check CPU load after 5 seconds.";
    }
    // end::getCpuLoad[]

    // tag::getMemoryUsage[]
    @GET
    @Path("/systemLoad/memoryUsage")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMemoryUsage() {
        concurrencyBean.getMemoryUsage();
        return "Check memory usage after 5 seconds.";
    }
    // end::getMemoryUsage[]

    // tag::schedule[]
    @GET
    @Path("/schedule")
    @Produces(MediaType.TEXT_PLAIN)
    public String schedule() {
        return String.valueOf(concurrencyBean.isScheduleStarted());
    }
    // end::schedule[]

    // tag::schedulToggle[]
    @GET
    @Path("/schedule/toggle")
    @Produces(MediaType.TEXT_PLAIN)
    public String schedulToggle() {
        if (concurrencyBean.isScheduleStarted()) {
            concurrencyBean.stopSchedule();
            return "Disabling the schedule...";
        } else {
            concurrencyBean.startSchedule();
            concurrencyBean.schedule();
            return "Enabling the schedule...";
        }
    }
    // end::schedulToggle[]
}
