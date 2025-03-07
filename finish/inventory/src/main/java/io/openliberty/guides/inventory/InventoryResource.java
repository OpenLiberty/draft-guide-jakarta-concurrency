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
import jakarta.transaction.Transactional;
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

    // tag::inventoryAsyncTask[]
    @Inject
    private InventoryAsyncTask task;
    // end::inventoryAsyncTask[]

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

    // tag::addSystemClient[]
    @POST
    @Path("/system/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addSystemClient(
        @Parameter(
           name = "hostname", in = ParameterIn.PATH,
           description = "the hostname of the system",
           required = true, example = "localhost",
           schema = @Schema(type = SchemaType.STRING))
        @PathParam("hostname") String hostname) {
        // tag::getClientData[]
        SystemData system = task.getClientData(hostname);
        if (system == null) {
            return fail("Failed to get data from " + hostname);
        }
        if (manager.add(system)) {
            return success(hostname + " was added.");
        } else {
            return fail(hostname + " already exists.");
        }
        // end::getClientData[]
    }
    // end::addSystemClient[]

    // tag::updateMemoryUsed[]
    @PUT
    @Path("/systems/memoryUsed")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMemoryUsed(
        @Parameter(
            name = "after", in = ParameterIn.QUERY,
            description = "update the memory usage after the specified seconds",
            required = true, example = "5",
            schema = @Schema(type = SchemaType.INTEGER))
        @QueryParam("after") Integer after) {
        // tag::updateSystemsMemoryUsed[]
        task.updateSystemsMemoryUsed(manager.getSystems(), after.intValue());
        // end::updateSystemsMemoryUsed[]
        return success("Check after " + after + " seconds");
    }
    // end::updateMemoryUsed[]

    // tag::updateSystemLoad[]
    @PUT
    @Path("/systems/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSystemLoad(
        @Parameter(
            name = "after", in = ParameterIn.QUERY,
            description = "update the system load after the specified seconds",
            required = true, example = "5",
            schema = @Schema(type = SchemaType.INTEGER))
        @QueryParam("after") Integer after) {
        List<SystemData> systems = manager.getSystems();
        // tag::countDownLatch[]
        CountDownLatch remainingSystems = new CountDownLatch(systems.size());
        // end::countDownLatch[]
        // tag::getSystemLoad[]
        for (SystemData s : systems) {
            task.getSystemLoad(s.getHostname(), after.intValue())
        // end::getSystemLoad[]
                // tag::thenAcceptAsync[]
                .thenAcceptAsync(systemLoad -> {
                       // tag::setSystemLoad[]
                       s.setSystemLoad(systemLoad);
                       // end::setSystemLoad[]
                       // tag::countDown1[]
                       remainingSystems.countDown();
                       // end::countDown1[]
                    })
                // end::thenAcceptAsync[]
                // tag::exceptionally[]
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    // tag::countDown2[]
                    remainingSystems.countDown();
                    // end::countDown2[]
                    return null;
               });
               // end::exceptionally[]
            }
        try {
            // tag::await[]
            remainingSystems.await(30, TimeUnit.SECONDS);
            // end::await[]
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success("Successfully updated the system load.");
    }
    // end::updateSystemLoad[]

    @DELETE
    @Path("/system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
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
        manager.init();
        return success("Reset the systems.");
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
