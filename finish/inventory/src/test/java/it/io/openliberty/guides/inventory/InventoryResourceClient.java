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
package it.io.openliberty.guides.inventory;

import java.util.List;

import io.openliberty.guides.inventory.models.SystemData;
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

@Path("/inventory")
public interface InventoryResourceClient {

    // tag::listContents[]
    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON)
    List<SystemData> listContents();
    // end::listContents[]

    // tag::getSystem[]
    @GET
    @Path("/system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    SystemData getSystem(@PathParam("hostname") String hostname);
    // end::getSystem[]

    // tag::addSystemClient[]
    @POST
    @Path("/system/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response addSystemClient(@PathParam("hostname") String hostname);
    // end::addSystemClient[]

    // tag::updateMemoryUsed[]
    @PUT
    @Path("/systems/memoryUsed")
    @Produces(MediaType.APPLICATION_JSON)
    Response updateMemoryUsed(@QueryParam("after") Integer after);
    // end::updateMemoryUsed[]

    // tag::updateSystemLoad[]
    @PUT
    @Path("/systems/systemLoad")
    @Produces(MediaType.APPLICATION_JSON)
    Response updateSystemLoad(@QueryParam("after") Integer after);
    // end::updateSystemLoad[]

    // tag::removeSystem[]
    @DELETE
    @Path("/system/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    Response removeSystem(@PathParam("hostname") String hostname);
    // end::removeSystem[]

    // tag::resetSystems[]
    @PUT
    @Path("/systems/reset")
    @Produces(MediaType.APPLICATION_JSON)
    Response resetSystems();
    // end::resetSystems[]

}
