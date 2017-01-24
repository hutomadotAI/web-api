document.getElementById("btnPublishDeveloper").addEventListener("click", checkDevInput);

document.getElementById("developer_name").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_email").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_address").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_postCode").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_city").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_country").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_company").addEventListener("keydown",function(){removeAlert(this)}, false);

function checkDevInput(){
    // Missing - developer name input validation
    // Missing - Developer address input validation
    // Missing - Developer city input validation
    // Missing - Developer country input validation
    // Missing - Developer company input validation
    // Missing - Developer website input validation

    // Developer email input validation
    var developer_email = document.getElementById('developer_email');
    if (developer_email.value != '' && developer_email.value !== 'undefined') {
        if (inputValidation(developer_email.value, 'developer_email')) {
            createAlertMessage(ALERT.DANGER.value, 'Please enter a valid email.','developer_email');
            return false;
        }
    }

    // block submit request
    $(this).prop("disabled", true);
    requestDevPublish();
}

function requestDevPublish(){
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnPublishDeveloper").prop("disabled", true);

    if (!fieldsDevValidation()){
        $("#btnPublishDeveloper").prop("disabled", false);
        document.body.style.cursor = '';
        return false;
    }
    createAlertMessage(ALERT.WARNING.value, 'Sending request...');
    var devInfo = fieldsToDevInstance();
    var jsonString = JSON.stringify(devInfo);

    $.ajax({
        url: './dynamic/updateDeveloper.php',
        data: {'developer' : jsonString},
        cache: false,
        type: 'POST',
        success: function (response) {
            var statusCode = JSON.parse(response);
            switch(statusCode['status']['code']){
                case 200:
                    // UPDATED developer
                    createAlertMessage(ALERT.PRIMARY.value, 'Request submitted!');
                    $("#btnPublishDeveloper").prop("disabled", false);
                    updateButton();
                    break;
                case 400:
                    // UPDATED not complete
                    createAlertMessage(ALERT.DANGER.value,'At least one of the required parameters is null or empty');
                    $("#btnPublishDeveloper").prop("disabled", false);
                    break;
                case 500:
                    // DEVELOPER JUST EXISTS
                    createAlertMessage(ALERT.WARNING.value,'Developer info sent');
                    break;
            }
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            $("#btnPublishDeveloper").prop("disabled", false);
            createAlertMessage(ALERT.DANGER.value, 'Request not sended!');
        }
    });

}

function removeAlert(node){
    if( node.getAttribute("style") != null && node.getAttribute("style")!="" ) {
        node.style.border = "0px";
        if( document.getElementById('containerMsgAlertPublish') !== null )
            document.getElementById('containerMsgAlertPublish').remove();
    }
}


function createAlertMessage(alarm,message,id) {
    var msg_class;
    var ico_class;

    switch (alarm){
        case 0:
            msg_class = 'alert alert-dismissable flat alert-base no-margin';
            ico_class = 'icon fa fa-check';
            if (id!=null)
                document.getElementById(id).style.border ="0px";
            break;
        case 1:
            msg_class = 'alert alert-dismissable flat alert-warning no-margin';
            ico_class = 'icon fa fa-check';
            if (id!=null)
                document.getElementById(id).style.border ="1px solid orange";
            break;
        case 2:
            msg_class = 'alert alert-dismissable flat alert-danger no-margin';
            ico_class = 'icon fa fa-warning';
            if (id!=null)
                document.getElementById(id).style.border ="1px solid red";
            break;
        case 4:
            msg_class = 'alert alert-dismissable flat alert-primary no-margin';
            ico_class = 'icon fa fa-check';
            break;
    }

    if (document.getElementById('containerMsgAlertDeveloper')===null) {
        var wHTML = '';
        wHTML += '<button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>';
        wHTML += '<i class="'+ ico_class+'" id="iconAlertDeveloper"></i>';
        wHTML += '<span id="msgAlertDeveloper">'+message+'</span>';

        var newNode = document.createElement('div');
        newNode.setAttribute('class', msg_class);
        newNode.setAttribute('id', 'containerMsgAlertDeveloper');
        newNode.style.marginBottom ='15px';

        newNode.innerHTML = wHTML;
        document.getElementById('alertDeveloperMessage').appendChild(newNode);
    }
    else{
        document.getElementById('containerMsgAlertDeveloper').setAttribute('class',msg_class);
        document.getElementById('iconAlertDeveloper').setAttribute('class', ico_class);
        document.getElementById('msgAlertDeveloper').innerText = message;
    }

}

function updateButton(){
    var btnNode = document.getElementById('btnPublishDeveloper');
    btnNode.className = 'btn btn-primary pull-right flat';
    btnNode.innerHTML = '<b>Next</b>' + '<span class="fa fa-arrow-circle-right"></span>';
    btnNode.removeEventListener("click", checkDevInput);
    btnNode.onclick = function () {  location.href = "./publishAI.php"; };
}

function fieldsToDevInstance(){
    var dev={};
    dev['name'] = document.getElementById('developer_name').value;
    dev['email'] = document.getElementById('developer_email').value;
    dev['address'] = document.getElementById('developer_address').value;
    dev['postCode'] = document.getElementById('developer_postCode').value;
    dev['city'] =document.getElementById('developer_city').value;
    dev['country'] = document.getElementById('developer_country').value;
    dev['company'] = document.getElementById('developer_company').value;
    dev['website'] = document.getElementById('developer_website').value;
    return dev;
}

function fieldsDevValidation(){
    var elem;

    elem = document.getElementById('developer_name');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The name field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_address');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The address field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_postCode');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The postcode field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_city');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The city field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_country');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The country field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_email');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The email field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_company');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(ALERT.DANGER.value, 'The company field cannot is empty!');
        return false;
    }

    return true;
}