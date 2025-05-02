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

const source = new EventSource('http://localhost:9080/api/system/sse',
                               { withCredentials: true });
source.addEventListener('SystemLoad', systemLoadHandler);

function systemLoadHandler(event) {
  const data = JSON.parse(event.data)
  const statusLabel = document.getElementById('status')
  if (data.schedule == null || data.schedule) {
    statusLabel.textContent = data.schedule
      ? 'New system load will be boardcast in every 10 seconds.'
      : '...'
    const cpuLoad = data.cpuLoad == null ? '-' : data.cpuLoad.toFixed(7)
    const memoryUsage = data.memoryUsage == null ? '-' : data.memoryUsage.toFixed(2)
    const table = document.getElementById('systemLoadsTable')
    const row = table.insertRow(1)
    row.insertCell(0).innerHTML = data.time
    row.insertCell(1).innerHTML = cpuLoad
    row.insertCell(2).innerHTML = memoryUsage
  } else {
    statusLabel.textContent = '...'
  }
}

function cleanCall () {
  const table = document.getElementById('systemLoadsTable')
  for (let i = table.rows.length - 1; i > 0; i--) {
    table.deleteRow(i)
  }
}

// tag::refreshCpuLoadCall[]
async function refreshCpuLoadCall () {
  const statusLabel = document.getElementById('status')
  statusLabel.textContent = 'New CPU load will be boardcast after 5 seconds.'
  const response = await fetch('/api/system/systemLoad/cpuLoad')
  console.log(response.status)
}
// end::refreshCpuLoadCall[]

// tag::refreshMemoryUsageCall[]
async function refreshMemoryUsageCall () {
  const statusLabel = document.getElementById('status')
  statusLabel.textContent = 'New memory usage will be boardcast after 10 seconds.'
  const response = await fetch('/api/system/systemLoad/memoryUsage')
  console.log(response.status)
}
// end::refreshMemoryUsageCall[]

// tag::scheduleCall[]
async function scheduleCall () {
  const statusLabel = document.getElementById('status')
  statusLabel.textContent = 'Toggling the schedule...'
  const response = await fetch('/api/system/schedule/toggle')
  console.log(response.status)
}
// end::scheduleCall[]
