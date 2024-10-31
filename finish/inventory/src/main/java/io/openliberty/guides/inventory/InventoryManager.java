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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openliberty.guides.inventory.models.SystemData;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryManager {

    List<SystemData> systems =
        Collections.synchronizedList(new ArrayList<SystemData>());

    public List<SystemData> getSystems() {
        return this.systems;
    }

    public SystemData getSystem(String hostname) {
        for (SystemData s : this.systems) {
            if (s.getHostname().equalsIgnoreCase(hostname)) {
                return s;
            }
        }
        return null;
    }

    public boolean add(SystemData s) {
        if (getSystem(s.getHostname()) != null) {
            return false;
        }
        this.systems.add(s);
        return true;
    }

    public boolean removeSystem(String hostname) {
        SystemData s = getSystem(hostname);
        if (s == null) {
            return false;
        }
        this.systems.remove(s);
        return true;
    }

}
