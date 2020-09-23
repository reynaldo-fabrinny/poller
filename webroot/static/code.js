//import {cookieName, setCookie, getCookie} from "./cookiesUtils.js";

const $tableBody = $("#table-body");
const urlRegex = /(http|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&:\/~+#-]*[\w@?^=%&\/~+#-])?/;
let servicesRequest = new Request('/service');

fetch(servicesRequest)
    .then(function (response) {
        return response.json();
    })
    .then(function (serviceList) {
        serviceList.forEach(service => {

            //    let cookieUserId = getCookie(cookieName);
            //  if (cookieUserId === service.user_cookie_id) {
            let $tr = $('<tr/>').addClass("d-flex");

            let $tdUrl = $('<td/>');
            $tdUrl.addClass("col-4");

            let $inputUrl = $("<input style='border: none;' required/>");
            $inputUrl.attr("disabled", true);
            $inputUrl.val(service.url);
            $tdUrl.append($inputUrl);

            $tr.append($tdUrl);
            let $tdName = $('<td/>');

            $tdName.addClass("col-3");

            let $inputName = $("<input style='border: none;' />");
            $inputName.attr("disabled", true);
            $inputName.val(service.name);

            $tdName.append($inputName);
            $tr.append($tdName);

            let $tdStatus = $('<td/>');
            $tdStatus.addClass("col-1");
            if (service.status_response === 200) {
                $tdStatus.append("OK");
            } else if (service.status_response) {
                $tdStatus.append("FAIL");
            } else if (service.status_response === -1) {
                $tdStatus.append("HOST UNKNOWN");
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


            let $editBtn = $('<button/>')
                .text('Edit')
                .attr("service-id", service.id)
                .click(function () {
                    $(this).toggle();
                    $saveBtn.toggle();
                    $inputName.attr("disabled", false);
                    $inputUrl.attr("disabled", false);

                }).addClass("btn btn-sm btn-secondary ml-2");

            $tdButton.append($editBtn);


            let $saveBtn = $('<button/>')
                .text('Save')
                .attr("service-id", service.id)
                .click(function () {
                    if (isValidUrl($inputUrl.val())) {

                        $(this).toggle();
                        $editBtn.toggle();
                        $inputName.attr("disabled", true);
                        $inputUrl.attr("disabled", true);

                        let serviceId = $(this).attr("service-id");
                        fetch('/service', {
                            method: 'put',
                            headers: {
                                'Accept': 'application/json, text/plain, */*',
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                id: serviceId,
                                name: $inputName.val(),
                                url: $inputUrl.val()
                            })
                        }).then(res => location.reload());
                    } else {
                        alert("invalid url");
                    }
                }).addClass("btn btn-sm btn-primary ml-2");
            $saveBtn.hide();
            $tdButton.append($saveBtn);

            $tr.append($tdButton);
            $tableBody.append($tr);
            //   }
        });
    });

$('#post-service').click(function () {
    let serviceUrl = document.querySelector('#service-url').value;
    let serviceName = document.querySelector('#service-name').value;

    /* let cookie = getCookie(cookieName);
     if (!cookie) {
         cookie = setCookie(cookieName);
     }
     cookie += "";*/

    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
//        body: JSON.stringify({url: serviceUrl, name: serviceName, user_cookie_id: cookie})
        body: JSON.stringify({url: serviceUrl, name: serviceName})
    }).then(res => location.reload());
});


$("#new-service-form").validate({
    rules: {
        "service-url": {
            required: true,
            url: true
        }
    },
    messages: {
        "service-url": {
            required: "The service url cannot be empty.",
            url: "Please enter a valid URL."
        }
    }
});

function isValidUrl(url) {
    return urlRegex.test(url)
}