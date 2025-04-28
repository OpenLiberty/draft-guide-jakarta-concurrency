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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

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

    // tag::getSystemLoad[]
    @GET
    @Path("/systemLoad/{after}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSystemLoad(@PathParam("after") int after) {
        bean.getSystemLoad(after);
        return "Check after " + after + " seconds.";
    }
    // end::getSystemLoad[]

    @GET
    @Path("/schedule")
    @Produces(MediaType.TEXT_PLAIN)
    public String schedule() {
        return String.valueOf(bean.isScheduleEnabled());
    }

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

    @GET
    @Path("/sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribe(@Context SseEventSink sink, @Context Sse sse) {
        bean.subscribe(sink, sse);
    }

}
