document.getElementById("btnPublishDeveloper").addEventListener("click", checkDevInput);

document.getElementById("developer_name").addEventListener("keydown", function () {
    removeAlert(this)
}, false);
document.getElementById("developer_email").addEventListener("keydown", function () {
    removeAlert(this)
}, false);
document.getElementById("developer_address").addEventListener("keydown", function () {
    removeAlert(this)
}, false);
document.getElementById("developer_postCode").addEventListener("keydown", function () {
    removeAlert(this)
}, false);
document.getElementById("developer_city").addEventListener("keydown", function () {
    removeAlert(this)
}, false);
document.getElementById("developer_website").addEventListener("keydown", function () {
    removeAlert(this)
}, false);
document.getElementById("developer_company").addEventListener("keydown", function () {
    removeAlert(this)
}, false);

function checkDevInput() {
    // Missing - Developer name input validation
    // Missing - Developer address input validation
    // Missing - Developer city input validation
    // Missing - Developer company input validation

    // Developer email input validation
    var developer_email = document.getElementById('developer_email');
    if (developer_email.value !== '' && developer_email.value !== 'undefined') {
        if (isInputInvalid(developer_email.value, 'developer_email')) {
            createAlertMessage(ALERT.DANGER.value, 'Please enter a valid email.', 'developer_email');
            return false;
        }
    }
    // Developer website input validation
    var developer_website = document.getElementById('developer_website');
    if (developer_website.value !== '' && developer_website.value !== 'undefined') {
        if (isInputInvalid(developer_website.value, 'URI')) {
            createAlertMessage(ALERT.DANGER.value, 'Please enter a valid URI.', 'developer_website');
            return false;
        }
    }

    // block submit request
    $(this).prop("disabled", true);
    requestDevPublish();
}

function requestDevPublish() {
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnPublishDeveloper").prop("disabled", true);

    if (!fieldsDevValidation()) {
        $("#btnPublishDeveloper").prop("disabled", false);
        document.body.style.cursor = '';
        return false;
    }
    createAlertMessage(ALERT.WARNING.value, 'Sending request...', null);
    var devInfo = fieldsToDevInstance();
    var jsonString = JSON.stringify(devInfo);

    $.ajax({
        url: './proxy/updateDeveloper.php',
        data: {'developer': jsonString},
        cache: false,
        type: 'POST',
        success: function (response) {
            var statusCode = JSON.parse(response);
            switch (statusCode['status']['code']) {
                case 200:
                    // UPDATED developer
                    $("#btnPublishDeveloper").prop("disabled", true);
                    callback();
                    break;
                case 400:
                    // UPDATED not complete
                    createAlertMessage(ALERT.DANGER.value, 'At least one of the required parameters is null or empty');
                    $("#btnPublishDeveloper").prop("disabled", false);
                    document.body.style.cursor = prevCursor;
                    break;
                case 500:
                    // DEVELOPER JUST EXISTS
                    createAlertMessage(ALERT.WARNING.value, 'Developer info sent');
                    document.body.style.cursor = prevCursor;
                    break;
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            $("#btnPublishDeveloper").prop("disabled", false);
            createAlertMessage(ALERT.DANGER.value, 'Request not sent!', null);
        }
    });

}

function callback() {
    location.href = "./publishAI.php";
}

function removeAlert(node) {
    if (node.getAttribute("style") !== null && node.getAttribute("style") !== "") {
        node.style.border = "0px";
        if (document.getElementById('containerMsgAlertDeveloper') !== null)
            document.getElementById('containerMsgAlertDeveloper').remove();
    }
}


function createAlertMessage(alarm, message, id) {
    var msg_class;
    var ico_class;

    switch (alarm) {
        case ALERT.BASIC.value:
            msg_class = 'alert alert-dismissable flat alert-base no-margin';
            ico_class = 'icon fa fa-check';
            if (id !== null)
                document.getElementById(id).style.border = "0px";
            break;
        case ALERT.WARNING.value:
            msg_class = 'alert alert-dismissable flat alert-warning no-margin';
            ico_class = 'icon fa fa-check';
            if (id !== null)
                document.getElementById(id).style.border = "1px solid orange";
            break;
        case ALERT.DANGER.value:
            msg_class = 'alert alert-dismissable flat alert-danger no-margin';
            ico_class = 'icon fa fa-warning';
            if (id !== null)
                document.getElementById(id).style.border = "1px solid red";
            break;
        case ALERT.PRIMARY.value:
            msg_class = 'alert alert-dismissable flat alert-primary no-margin';
            ico_class = 'icon fa fa-check';
            break;
    }

    if (document.getElementById('containerMsgAlertDeveloper') === null) {
        var wHTML = '';
        wHTML += '<button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>';
        wHTML += '<i class="' + ico_class + '" id="iconAlertDeveloper"></i>';
        wHTML += '<span id="msgAlertDeveloper">' + message + '</span>';

        var newNode = document.createElement('div');
        newNode.setAttribute('class', msg_class);
        newNode.setAttribute('id', 'containerMsgAlertDeveloper');
        newNode.style.marginBottom = '15px';

        newNode.innerHTML = wHTML;
        document.getElementById('alertDeveloperMessage').appendChild(newNode);
    }
    else {
        document.getElementById('containerMsgAlertDeveloper').setAttribute('class', msg_class);
        document.getElementById('iconAlertDeveloper').setAttribute('class', ico_class);
        document.getElementById('msgAlertDeveloper').innerText = message;
    }

}

function updateButton() {
    var btnNode = document.getElementById('btnPublishDeveloper');
    btnNode.className = 'btn btn-primary pull-right flat';
    btnNode.innerHTML = '<b>Next </b>' + '<span class="fa fa-arrow-circle-right"></span>';
    btnNode.removeEventListener("click", checkDevInput);
    btnNode.onclick = function () {
        location.href = "./publishAI.php";
    };
}

function fieldsToDevInstance() {
    var dev = {};
    dev['name'] = document.getElementById('developer_name').value;
    dev['email'] = document.getElementById('developer_email').value;
    dev['address'] = document.getElementById('developer_address').value;
    dev['postCode'] = document.getElementById('developer_postCode').value;
    dev['city'] = document.getElementById('developer_city').value;
    dev['country'] = document.getElementById('developer_country').value;
    dev['company'] = document.getElementById('developer_company').value;
    dev['website'] = document.getElementById('developer_website').value;
    return dev;
}

function fieldsDevValidation() {
    var elem;

    elem = document.getElementById('developer_name');
    if (elem.value === '') {
        elem.style.border = "1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The name field cannot be empty!', 'developer_name');
        return false;
    }

    elem = document.getElementById('developer_address');
    if (elem.value === '') {
        elem.style.border = "1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The address field cannot be empty!', 'developer_address');
        return false;
    }

    elem = document.getElementById('developer_postCode');
    if (elem.value === '') {
        elem.style.border = "1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The postcode field cannot be empty!', 'developer_postCode');
        return false;
    }

    elem = document.getElementById('developer_city');
    if (elem.value == '') {
        elem.style.border = "1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The city field cannot be empty!', 'developer_city');
        return false;
    }

    elem = document.getElementById('developer_email');
    if (elem.value === '') {
        elem.style.border = "1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The email field cannot be empty!', 'developer_email');
        return false;
    }

    elem = document.getElementById('developer_company');
    if (elem.value === '') {
        elem.style.border = "1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The company field cannot be empty!', 'developer_company');
        return false;
    }

    return true;
}