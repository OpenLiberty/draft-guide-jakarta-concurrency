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

import io.openliberty.guides.inventory.models.SystemData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class InventoryManager {

    @PersistenceContext(name = "jpa-unit")
    private EntityManager em;

    List<SystemData> systems = null;

    public List<SystemData> getSystems() {
        if (systems == null) {
            systems = em.createNamedQuery("SystemData.findAll", SystemData.class)
                .getResultList();
        }
        return systems;
    }

    public SystemData getSystem(String hostname) {
        List<SystemData> systems =
                em.createNamedQuery("SystemData.findSystem", SystemData.class)
                  .setParameter("hostname", hostname)
                  .getResultList();
        return systems == null || systems.isEmpty() ? null : systems.get(0);
    }

    public boolean add(SystemData system) {
        if (getSystem(system.getHostname()) != null) {
            return false;
        }
        em.persist(system);
        getSystems().add(system);
        return true;
    }

    public boolean removeSystem(String hostname) {
        SystemData s = getSystem(hostname);
        if (s == null) {
            return false;
        }
        em.remove(s);
        getSystems().remove(s);
        return true;
    }

}
