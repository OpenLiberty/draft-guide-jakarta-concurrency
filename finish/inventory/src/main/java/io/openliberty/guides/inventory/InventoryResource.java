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

import java.net.URI;
import java.util.List;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUriExceptionMapper;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.guides.inventory.model.SystemData;
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
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
@Path("/systems")
public class InventoryResource {

    @Inject
    InventoryManager inventoryManager;

    @Inject
    InventoryAsyncTask inventoryAsyncTask;

    @Inject
    @ConfigProperty(name = "client.https.port")
    String CLIENT_PORT;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SystemData> listContents() {
        return inventoryManager.getSystems();
    }

    @GET
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemData getSystem(
        @PathParam("hostname") String hostname) {
        return inventoryManager.getSystem(hostname);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addSystem(
        @QueryParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventoryManager.getSystem(hostname);
        if (s != null) {
            return fail(hostname + " already exists.");
        }
        inventoryManager.add(hostname, osName, javaVersion, heapSize);
        return success(hostname + " was added.");
    }

    @PUT
    @Path("/{hostname}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateSystem(
        @PathParam("hostname") String hostname,
        @QueryParam("osName") String osName,
        @QueryParam("javaVersion") String javaVersion,
        @QueryParam("heapSize") Long heapSize) {

        SystemData s = inventoryManager.getSystem(hostname);
        if (s == null) {
            return fail(hostname + " does not exists.");
        }
        s.setOsName(osName);
        s.setJavaVersion(javaVersion);
        s.setHeapSize(heapSize);
        inventoryManager.update(s);
        return success(hostname + " was updated.");
    }

    @DELETE
    @Path("/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response removeSystem(@PathParam("hostname") String hostname) {
        SystemData s = inventoryManager.getSystem(hostname);
        if (s != null) {
            inventoryManager.removeSystem(s);
            return success(hostname + " was removed.");
        } else {
            return fail(hostname + " does not exists.");
        }
    }

    @POST
    @Path("/client/{hostname}")
    // end::addSystemClient[]
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SystemData addSystemClient(@PathParam("hostname") String hostname){

        SystemData systemData;
        try {
            systemData = inventoryAsyncTask.createSystemData(hostname, CLIENT_PORT);
            inventoryManager.add(systemData);
        } catch (Exception e) {
            throw new WebApplicationException
                    ("Failed to create system data for " + hostname + ".");
        }

        return systemData;
    }

    private Response success(String message) {
        return Response.ok("{ \"ok\" : \"" + message + "\" }").build();
    }

    private Response fail(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
               .entity("{ \"error\" : \"" + message + "\" }")
               .build();
    }

    private SystemClient getSystemClient(String hostname) throws Exception {
        String customURIString = "https://" + hostname + ":" + CLIENT_PORT + "/system";
        URI customURI = URI.create(customURIString);
        return RestClientBuilder.newBuilder()
                .baseUri(customURI)
                .register(UnknownUriExceptionMapper.class)
                .build(SystemClient.class);
    }


}