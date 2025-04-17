const webSocket = new WebSocket('ws://localhost:9080/systemLoad')

webSocket.onopen = function (event) {
    console.log(event);
};

webSocket.onmessage = function (event) {
    var data = JSON.parse(event.data);
    var tableRow = document.createElement('tr');
    var cpuLoad = data.cpuLoad == null ? '-' : data.cpuLoad.toFixed(7);
    var memoryUsage = data.memoryUsage == null ? '-' : data.memoryUsage.toFixed(2);
    tableRow.innerHTML = '<td>' + data.time + '</td>' +
                         '<td>' + cpuLoad + '</td>' +
                         '<td>' + memoryUsage + '</td>';
    document.getElementById('systemLoadsTableBody').appendChild(tableRow);
};

webSocket.onerror = function (event) {
    console.log(event);
};

async function refreshCall() {
    var response = await fetch("/api/system/refresh/5");
    console.log(response.status)
}

async function scheduleCall() {
    var response = await fetch("/api/system/schedule");
    console.log(response.status)
}