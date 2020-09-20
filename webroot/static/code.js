const $tableBody = $("#table-body");
let servicesRequest = new Request('/service');

fetch(servicesRequest)
    .then(function (response) {
        return response.json();
    })
    .then(function (serviceList) {
        serviceList.forEach(service => {
            let $tr = $('<tr/>').addClass("d-flex");

            let $tdUrl = $('<td/>');
            $tdUrl.addClass("col-4").append(service.url);
            $tr.append($tdUrl);

            let $tdName = $('<td/>');
            $tdName.addClass("col-3").append(service.name);
            $tr.append($tdName);

            let $tdStatus = $('<td/>');
            $tdStatus.addClass("col-1");
            if (service.status_response === 200) {
                $tdStatus.append("OK");
            } else if (service.status_response) {
                $tdStatus.append("FAIL");
            } else {
                $tdStatus.append("NOT CHECKED YET");
            }
            $tr.append($tdStatus);

            let $tdCreationDate = $('<td/>');
            $tdCreationDate.addClass("col-2");
            $tdCreationDate.append(service.creation_date);
            $tr.append($tdCreationDate);

            let $tdButton = $('<td/>');
            let $deleteBtn = $('<button/>')
                .text('Remove')
                .attr("service-id", service.id)
                .click(function () {
                    let serviceId = $(this).attr("service-id");
                    fetch('/remove/' + serviceId, {
                        method: 'delete',
                        headers: {
                            'Accept': 'application/json, text/plain, */*',
                            'Content-Type': 'application/json'
                        }
                    }).then(res => location.reload());
                }).addClass("btn btn-sm btn-danger");
            $tdButton.append($deleteBtn);
            $tr.append($tdButton);

            $tableBody.append($tr);
        });
    });

$('#post-service').click(function () {
    let serviceUrl = document.querySelector('#service-url').value;
    let serviceName = document.querySelector('#service-name').value;
    fetch('/add', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({url: serviceUrl, name: serviceName})
    }).then(res => location.reload());
});

$(document).ready(function(){

});