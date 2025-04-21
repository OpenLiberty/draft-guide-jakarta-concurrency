const webSocket = new WebSocket('ws://localhost:9080/systemLoad')

webSocket.onopen = function (event) {
    console.log(event);
};

webSocket.onmessage = function (event) {
    var data = JSON.parse(event.data);
    console.log(data.schedule);
    var statusLabel = document.getElementById('status');
    if (data.schedule == null || data.schedule) {
        statusLabel.textContent = data.schedule ?
            "New system load will be boardcast in every 10 seconds." : "...";
        var tableRow = document.createElement('tr');
        var cpuLoad = data.cpuLoad == null ? '-' : data.cpuLoad.toFixed(7);
        var memoryUsage = data.memoryUsage == null ? '-' : data.memoryUsage.toFixed(2);
        tableRow.innerHTML = '<td>' + data.time + '</td>' +
                             '<td>' + cpuLoad + '</td>' +
                             '<td>' + memoryUsage + '</td>';
        document.getElementById('systemLoadsTableBody').appendChild(tableRow);
    } else {
        statusLabel.textContent = "...";
    }
};

webSocket.onerror = function (event) {
    console.log(event);
};

function cleanCall() {
    var tBody = document.getElementById('systemLoadsTableBody');
    for (var i = tBody.rows.length - 1; i > 0; i--) {
        tBody.deleteRow(i);
    }
}

async function refreshCall() {
    var statusLabel = document.getElementById('status');
    statusLabel.textContent = "New system load will be boardcast after 5 seconds.";
    var response = await fetch("/api/system/refresh/5");
    console.log(response.status)
}

async function scheduleCall() {
    var statusLabel = document.getElementById('status');
    statusLabel.textContent = "Toggling the schedule...";
    var response = await fetch("/api/system/schedule");
    console.log(response.status);
}