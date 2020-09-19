const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
    .then(function (response) {
        return response.json();
    })
    .then(function (serviceList) {
        serviceList.forEach(service => {
            var li = document.createElement("li");
            var serviceRow = "URL: " + service.url;

            if (service.name) {
                serviceRow += " | Name: " + service.name;
            }
            if (service.status_response === 0) {
                serviceRow += " | Status Response: OK";
            } else if (service.status_response === 1) {
                serviceRow += " | Status Response: FAIL";
            } else {
                serviceRow += " | Status Response: NOT CHECKED YET";
            }
            if (service.creation_date) {
                serviceRow += " | Creation Date: " + service.creation_date;
            }
            li.appendChild(document.createTextNode(serviceRow));
            listContainer.appendChild(li);
        });
    });

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let serviceUrl = document.querySelector('#service-url').value;
    let serviceName = document.querySelector('#service-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({url: serviceUrl, name: serviceName})
    }).then(res => location.reload());
}