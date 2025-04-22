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
        var cpuLoad = data.cpuLoad == null ? '-' : data.cpuLoad.toFixed(7);
        var memoryUsage = data.memoryUsage == null ? '-' : data.memoryUsage.toFixed(2);
        var table = document.getElementById('systemLoadsTable');
        var row = table.insertRow(1);
        row.insertCell(0).innerHTML = data.time;
        row.insertCell(1).innerHTML = cpuLoad;
        row.insertCell(2).innerHTML = memoryUsage;
    } else {
        statusLabel.textContent = "...";
    }
};

webSocket.onerror = function (event) {
    console.log(event);
};

function cleanCall() {
    var table = document.getElementById('systemLoadsTable');
    for (var i = table.rows.length - 1; i > 0; i--) {
        table.deleteRow(i);
    }
}

async function refreshCall() {
    var statusLabel = document.getElementById('status');
    statusLabel.textContent = "New system load will be boardcast after 5 seconds.";
    var response = await fetch("/api/system/systemLoad/5");
    console.log(response.status)
}

async function scheduleCall() {
    var statusLabel = document.getElementById('status');
    statusLabel.textContent = "Toggling the schedule...";
    var response = await fetch("/api/system/schedule");
    console.log(response.status);
}