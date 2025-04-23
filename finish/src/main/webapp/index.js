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
const webSocket = new WebSocket('ws://localhost:9080/systemLoad')

webSocket.onopen = function (event) {
  console.log(event)
}

webSocket.onmessage = function (event) {
  let data = JSON.parse(event.data)
  console.log(data.schedule)
  let statusLabel = document.getElementById('status')
  if (data.schedule == null || data.schedule) {
    statusLabel.textContent = data.schedule
      ? 'New system load will be boardcast in every 10 seconds.'
	  : '...'
    let cpuLoad = data.cpuLoad == null ? '-' : data.cpuLoad.toFixed(7)
    let memoryUsage = data.memoryUsage == null ? '-' : data.memoryUsage.toFixed(2)
    let table = document.getElementById('systemLoadsTable')
    let row = table.insertRow(1)
    row.insertCell(0).innerHTML = data.time
    row.insertCell(1).innerHTML = cpuLoad
    row.insertCell(2).innerHTML = memoryUsage
  } else {
    statusLabel.textContent = '...'
  }
}

webSocket.onerror = function (event) {
  console.log(event)
}

function cleanCall() {
  let table = document.getElementById('systemLoadsTable')
  for (var i = table.rows.length - 1; i > 0; i--) {
    table.deleteRow(i)
  }
}

async function refreshCall() {
  let statusLabel = document.getElementById('status')
  statusLabel.textContent = 'New system load will be boardcast after 5 seconds.'
  let response = await fetch('/api/system/systemLoad/5')
  console.log(response.status)
}

async function scheduleCall() {
  let statusLabel = document.getElementById('status')
  statusLabel.textContent = 'Toggling the schedule...'
  let response = await fetch('/api/system/schedule')
  console.log(response.status)
}