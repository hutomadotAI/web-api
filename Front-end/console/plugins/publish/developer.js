document.getElementById("btnPublishDeveloper").addEventListener("click", checkDevInput);

document.getElementById("developer_name").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_email").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_address").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_postcode").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_city").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_country").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("developer_company").addEventListener("keydown",function(){removeAlert(this)}, false);


setDeveloperJustForDebug();
// TODO remove
function setDeveloperJustForDebug(){
    document.getElementById('developer_name').value=('hu:toma Ltd.');
    document.getElementById('developer_company').value=('HUTOMA');
    document.getElementById('developer_email').value=('support@hutoma.com');
    document.getElementById('developer_address').value= ('Carrer del Consell de Cent, 341');
    document.getElementById('developer_postcode').value=('08007');
    document.getElementById('developer_city').value= ('Barcelona');
    document.getElementById('developer_country').value=('Spain');
    document.getElementById('developer_website').value=('http://www.hutoma.com');
}


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
            createAlertMessage(2, 'Please enter a valid email.','developer_email');
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

    if (!fieldsValidation()){
        $("#btnPublishDeveloper").prop("disabled", false);
        document.body.style.cursor = '';
        return false;
    }
    createAlertMessage(1, 'Sending request...');

    if(document.publishDeveloperForm.onsubmit)
        return;
    
    RecursiveUnbind($('#wrapper'));
    document.publishDeveloperForm.submit();
    
    /*
    var developer = fieldsDevToArray();
    $.ajax({
        url: './dynamic/updateDeveloper.php',
        data: {
            name: developer['name'],
            email: developer['email'],
            address: developer['address'],
            postcode: developer['postcode'],
            city: developer['city'],
            country: developer['country'],
            company: developer['company'],
            website: developer['website']
        },
        type: 'POST',
        success: function (response) {
            createAlertMessage(4, 'Request submitted!');
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
            $("#btnPublishDeveloper").prop("disabled", false);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            $("#btnPublishDeveloper").prop("disabled", false);
            createAlertMessage(2, 'Request not sended!');
        }
    });
    */
}


function removeAlert(node){
    if( node.getAttribute("style") != null && node.getAttribute("style")!="" ) {
        node.style.border = "0px";
        if( document.getElementById('containerMsgAlertPublish') !== null )
            document.getElementById('containerMsgAlertPublish').remove();
    }
}

function fieldsValidation(){
    var elem;

    elem = document.getElementById('developer_name');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The name field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_address');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The address field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_postcode');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The postcode field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_city');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The city field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_country');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The country field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_email');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The email field cannot is empty!');
        return false;
    }

    elem = document.getElementById('developer_company');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The company field cannot is empty!');
        return false;
    }

    return true;
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

function fieldsDevToArray(){
    var tmp_dev =[];
    tmp_dev['name'] = document.getElementById('developer_name').value;
    tmp_dev['email'] = document.getElementById('developer_email').value;
    tmp_dev['address'] = document.getElementById('developer_address').value;
    tmp_dev['postcode'] = document.getElementById('developer_postcode').value;
    tmp_dev['city'] = document.getElementById('developer_city').value;
    tmp_dev['country'] = document.getElementById('developer_country').value;
    tmp_dev['company'] = document.getElementById('developer_company').value;
    tmp_dev['website'] = document.getElementById('developer_website').value;
    return tmp_dev;
}