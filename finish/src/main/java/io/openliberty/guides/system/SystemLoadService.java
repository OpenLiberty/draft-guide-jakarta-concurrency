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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint(value = "/systemLoad",
                decoders = { SystemLoadDecoder.class },
                encoders = { SystemLoadEncoder.class })
public class SystemLoadService {

    private static Logger logger = Logger.getLogger(SystemLoadService.class.getName());

    private Set<Session> sessions = new CopyOnWriteArraySet<Session>();

    public void sendToAllSessions(JsonObject systemLoad) {
        sessions.forEach(session -> {
            try {
                session.getBasicRemote().sendObject(systemLoad);
            } catch (Exception e) {
                logger.warning("Failed to send system load to " + 
                    session.getId() + ":" + e.getMessage());
            }
        });
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Server connected to session: " + session.getId());
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info("Session " + session.getId()
                    + " was closed with reason " + closeReason.getCloseCode());
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("WebSocket error for " + session.getId() + " "
                    + throwable.getMessage());
    }

}
