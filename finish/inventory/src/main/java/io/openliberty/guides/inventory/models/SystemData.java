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
package io.openliberty.guides.inventory.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "SystemData")
@NamedQuery(name = "SystemData.findAll", query = "SELECT sd FROM SystemData sd")
public class SystemData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "hostname")
    private String hostname;

    @Column(name = "osName")
    private String osName;

    @Column(name = "javaVersion")
    private String javaVersion;

    @Column(name = "heapSize")
    private Long   heapSize;

    private Double memoryUsage = 0.0;
    private Double systemLoad = 0.0;

    public SystemData() {
    }

    public SystemData(String hostname, String osName, String javaVer, Long heapSize) {
        this.hostname = hostname;
        this.osName = osName;
        this.javaVersion = javaVer;
        this.heapSize = heapSize;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOsName() {
        return this.osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getJavaVersion() {
        return this.javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public Long getHeapSize() {
        return this.heapSize;
    }

    public void setHeapSize(Long heapSize) {
        this.heapSize = heapSize;
    }

    public Double getSystemLoad() {
        return this.systemLoad;
    }

    public void setSystemLoad(Double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public Double getMemoryUsage() {
        return this.memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsage = ((double) memoryUsed) / this.heapSize;
    }

    @Override
    public int hashCode() {
        return hostname.hashCode();
    }

    public boolean equals(Object host) {
        if (host instanceof SystemData) {
            return this.hostname.equals(((SystemData) host).getHostname());
        }
        return false;
    }

}
