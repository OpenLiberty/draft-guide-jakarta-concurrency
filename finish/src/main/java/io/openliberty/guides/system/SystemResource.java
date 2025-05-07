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
    SystemConcurrency bean;

    @GET
    @Path("/properties/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getProperties(@PathParam("prefix") String prefix)
        throws InterruptedException, ExecutionException {
        return bean.getProperties(prefix);
    }

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SystemLoadData> getSystemLoads() {
        return bean.getSystemLoads();
    }

    // tag::getCpuLoad[]
    @GET
    @Path("/systemLoad/cpuLoad")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCpuLoad() {
        bean.getCpuLoad();
        return "Check CPU load after 5 seconds.";
    }
    // end::getCpuLoad[]

    // tag::getMemoryUsage[]
    @GET
    @Path("/systemLoad/memoryUsage")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMemoryUsage() {
        bean.getMemoryUsage();
        return "Check memory usage after 5 seconds.";
    }
    // end::getMemoryUsage[]

    // tag::schedule[]
    @GET
    @Path("/schedule")
    @Produces(MediaType.TEXT_PLAIN)
    public String schedule() {
        return String.valueOf(bean.isScheduleEnabled());
    }
    // end::schedule[]

    // tag::schedulToggle[]
    @GET
    @Path("/schedule/toggle")
    @Produces(MediaType.TEXT_PLAIN)
    public String schedulToggle() {
        if (bean.isScheduleEnabled()) {
            bean.enableSchedule(false);
            return "Disabling the schedule...";
        } else {
            bean.enableSchedule(true);
            bean.schedule();
            return "Enabling the schedule...";
        }
    }
    // end::schedulToggle[]
}
