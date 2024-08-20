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
package io.openliberty.guides.inventory.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/system")
public interface SystemClient extends AutoCloseable {

    @GET
    @Path("/property/{property}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProperty(@PathParam("property") String property);

    @GET
    @Path("/heapSize")
    @Produces(MediaType.TEXT_PLAIN)
    public Long getHeapSize();

    @GET
    @Path("/systemLoad")
    @Produces(MediaType.TEXT_PLAIN)
    public Double getSystemLoad();

    @GET
    @Path("/memoryUsed")
    @Produces(MediaType.TEXT_PLAIN)
    public Long getMemoryUsed();

}