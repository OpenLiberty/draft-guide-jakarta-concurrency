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

import io.openliberty.guides.inventory.models.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/inventory")
public class InventoryResource {

    @Inject
    private InventoryManager manager;

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
