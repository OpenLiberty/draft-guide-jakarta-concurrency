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

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

@ApplicationScoped
public class SseService {

    private static Logger logger = Logger.getLogger(SseService.class.getName());

    private Sse sse;
    private SseBroadcaster broadcaster;

    public void subscribe(SseEventSink sink, Sse sse) {
        if (this.sse == null || this.broadcaster == null) {
            this.sse = sse;
            this.broadcaster = sse.newBroadcaster();
        }
        this.broadcaster.register(sink);
    }

    public void broadcast(JsonObject data) {
        if (broadcaster != null) {
            OutboundSseEvent event = sse.newEventBuilder()
                                        .name("SystemLoad")
                                        .data(data.getClass(), data)
                                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                                        .build();
            broadcaster.broadcast(event);
        } else {
            logger.warning("Unable to send SSE. Broadcaster context is not set up.");
        }
    }

}
