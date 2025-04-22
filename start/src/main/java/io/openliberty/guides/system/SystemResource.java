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

import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    SystemConcurrency concurrenyBean;

    @GET
    @Path("/properties/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getProperties(@PathParam("prefix") String prefix)
        throws InterruptedException, ExecutionException {
         return concurrenyBean.getProperties(prefix);
    }

    @Path("/refresh/{after}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String refresh(@PathParam("after") Integer after) {
        concurrenyBean.refresh(after);
        return "Check after " + after + " seconds.";
    }

    @Path("/schedule")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String schedule() {
        if (SystemConcurrency.isScheduleEnabled()) {
            SystemConcurrency.enableSchedule(false);
            return "Disabling the schedule...";
        } else {
            SystemConcurrency.enableSchedule(true);
            concurrenyBean.schedule();
            return "Enabling the schedule...";
        }
    }

}
