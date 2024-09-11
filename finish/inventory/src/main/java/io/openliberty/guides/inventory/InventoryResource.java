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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import io.openliberty.guides.inventory.models.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/inventory")
public class InventoryResource {

    @Inject
    private InventoryManager manager;

    @Inject
    private InventoryAsyncTask task;

    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SystemData> listContents() {
        return manager.getSystems();
    }

    @GET
    @Path("/system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemData getSystem(@PathParam("hostname") String hostname) {
        return manager.getSystem(hostname);
    }

    @POST
    @Path("/system/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSystemClient(
        @Parameter(
           name = "hostname", in = ParameterIn.PATH,
           description = "the hostname of the system",
           required = true, example = "localhost",
           schema = @Schema(type = SchemaType.STRING))
        @PathParam("hostname") String hostname) {
        SystemData system = task.getClientData(hostname);
        if (system == null) {
            return fail("Failed to get data from " + hostname);
        }
        if (manager.add(system)) {
            return success(hostname + " was added.");
        } else {
            return fail(hostname + " already exists.");
        }
    }

    @DELETE
    @Path("/system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeSystem(@PathParam("hostname") String hostname) {
        if (manager.removeSystem(hostname)) {
            return success(hostname + " was removed.");
        }
        return fail("Failed to remove " + hostname);
    }

    @PUT
    @Path("/systems/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetSystems() {
        for (SystemData s : manager.getSystems()) {
            s.setSystemLoad(0.0);
            s.setMemoryUsage((long) 0);
        }
        return success("Reset the systems.");
    }

    @PUT
    @Path("/systems/memoryUsed")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMemoryUsed(
        @Parameter(
            name = "after", in = ParameterIn.QUERY,
            description = "to update the memory usage after the specified number of seconds",
            required = true, example = "5",
            schema = @Schema(type = SchemaType.INTEGER))
        @QueryParam("after") Integer after) {
        task.updateSystemsMemoryUsed(manager.getSystems(), after.intValue() * 1000);
        return success("Check after " + after + " seconds");
    }

    @PUT
    @Path("/systems/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSystemLoad(
        @Parameter(
            name = "after", in = ParameterIn.QUERY,
            description = "to update the system load after the specified number of seconds",
            required = true, example = "5",
            schema = @Schema(type = SchemaType.INTEGER))
        @QueryParam("after") Integer after) {
        List<SystemData> systems = manager.getSystems();
        CountDownLatch remainingSystems = new CountDownLatch(systems.size());
        for (SystemData s : systems) {
            task.getSystemLoad(s.getHostname(), after.intValue() * 1000)
                .thenAcceptAsync(systemLoad -> {
                       s.setSystemLoad(systemLoad);
                       remainingSystems.countDown();
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    remainingSystems.countDown();
                    return null;
               });
        }
        try {
            remainingSystems.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success("Successfully updated the system load.");
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"" + message + "\" }")
                       .build();
    }
}
