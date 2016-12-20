document.getElementById("btnPublishRequest").addEventListener("click", checkInput);

document.getElementById("bot_name").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_description").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_licence_fee").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_version").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_name").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_city").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_country").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_company").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_email").addEventListener("keydown",function(){removeAlert(this)}, false);

function checkInput(){
    // BOT name input validation
    var bot_name = document.getElementById('bot_name');
    if (bot_name.value !== 'undefined') {
        if (inputValidation(bot_name.value, 'bot_name')) {
            createAlertMessage(2, 'The AI name can only contains letters and numbers.','bot_name');
            return false;
        }
    }

    // BOT short description input validation
    var bot_description = document.getElementById('bot_description');
    if (bot_description.value != '' && bot_description.value !== 'undefined') {
        if (inputValidation(bot_description.value, 'bot_description')) {
            createAlertMessage(2, 'Invalid description text. Please enter a string that contains alphanumeric characters.','bot_description');
            return false;
        }
    }

    // BOT long description input validation
    // BOT usecase input validation
    // BOT alert Message input validation
    // BOT link privacy input validation

    // BOT licence fee ( price ) input validation
    var bot_licence_fee = document.getElementById('bot_licence_fee');
    if (bot_licence_fee.value != '' && bot_licence_fee.value !== 'undefined') {
        if (inputValidation(bot_licence_fee.value, 'bot_licence_fee')) {
            createAlertMessage(2, 'Please enter a valid number.','bot_licence_fee');
            return false;
        }
    }

    // BOT version input validation
    var bot_version = document.getElementById('bot_version');
    if (bot_version.value != '' && bot_licence_fee.value !== 'undefined') {
        if (inputValidation(bot_version.value, 'bot_version')) {
            createAlertMessage(2, 'Incorrect Version.','bot_version');
            return false;
        }
    }

    // BOT developer name input validation
    var developer_name = document.getElementById('bot_developer_name');
    if (developer_name.value != '' && developer_name.value !== 'undefined') {
        if (inputValidation(developer_name.value, 'developer_name')) {
            createAlertMessage(2, 'Please enter a name that contains alphanumeric characters','bot_developer_name');
            return false;
        }
    }
    
    // BOT developer address input validation

    // BOT developer city input validation
    var developer_city = document.getElementById('bot_developer_city');
    if (developer_city.value != '' && developer_city.value !== 'undefined') {
        if (inputValidation(developer_city.value, 'developer_city')) {
            createAlertMessage(2, 'The name need contains only a-z characters.','bot_developer_city');
            return false;
        }
    }

    // BOT developer country input validation
    var developer_country = document.getElementById('bot_developer_country');
    if (developer_country.value != '' && developer_country.value !== 'undefined') {
        if (inputValidation(developer_country.value, 'developer_country')) {
            createAlertMessage(2, 'The name need contains only a-z characters.','bot_developer_country');
            return false;
        }
    }
    // BOT developer email input validation
    var developer_email = document.getElementById('bot_developer_email');
    if (developer_email.value != '' && developer_email.value !== 'undefined') {
        if (inputValidation(developer_email.value, 'developer_email')) {
            createAlertMessage(2, 'Please enter a valid email.','bot_developer_email');
            return false;
        }
    }
    // BOT developer company input validation
    // BOT developer website input validation


    // block submit request
    $(this).prop("disabled", true);

    requestPublish();
}

function requestPublish(){
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnPublishRequest").prop("disabled", true);

    createAlertMessage(1, 'Sending request...');
    fieldsToBotIstance();
    $.ajax({
        url: './dynamic/publish.php',
        data: {
            bot: bot
        },
        type: 'POST',
        success: function (response) {
            createAlertMessage(4, 'Request submitted!');
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
            $("#btnPublishRequest").prop("disabled", false);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            $("#btnPublishRequest").prop("disabled", false);
            createAlertMessage(2, 'Request not sended!');
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
            msg_class = 'alert alert-dismissable flat alert-base';
            ico_class = 'icon fa fa-check';
            if (id!=null)
                document.getElementById(id).style.border ="0px";
            break;
        case 1:
            msg_class = 'alert alert-dismissable flat alert-warning';
            ico_class = 'icon fa fa-check';
            if (id!=null)
                document.getElementById(id).style.border ="1px solid orange";
            break;
        case 2:
            msg_class = 'alert alert-dismissable flat alert-danger';
            ico_class = 'icon fa fa-warning';
            if (id!=null)
                document.getElementById(id).style.border ="1px solid red";
            break;
        case 4:
            msg_class = 'alert alert-dismissable flat alert-primary';
            ico_class = 'icon fa fa-check';
            break;
    }

    if (document.getElementById('containerMsgAlertPublish')===null) {
        var wHTML = '';
        wHTML += '<button type="button" class="close text-white" data-dismiss="alert" aria-hidden="true">×</button>';
        wHTML += '<i class="'+ ico_class+'" id="iconAlertPublish"></i>';
        wHTML += '<span id="msgAlertPublish">'+message+'</span>';

        var newNode = document.createElement('div');
        newNode.setAttribute('class', msg_class);
        newNode.setAttribute('id', 'containerMsgAlertPublish');
        newNode.style.marginBottom ='15px';

        newNode.innerHTML = wHTML;
        document.getElementById('alertPublishMessage').appendChild(newNode);
    }
    else{
        document.getElementById('containerMsgAlertPublish').setAttribute('class',msg_class);
        document.getElementById('iconAlertPublish').setAttribute('class', ico_class);
        document.getElementById('msgAlertPublish').innerText = message;
    }

}

function populateBotFields(){
    document.getElementById('bot_name').value = bot['name'];
    document.getElementById('bot_description').value = bot['shortDescription'];
    document.getElementById('bot_long_description').innerText = bot['longDescription'];
    setSelectValue('bot_licence_type',bot['licenceType']);
    setSelectValue('bot_category',bot['category']);
    setSelectValue('bot_classification',bot['classification']);
    document.getElementById('bot_licence_fee').value = bot['licenceFee'];
    document.getElementById('bot_version').value = bot['version'];
    document.getElementById('bot_usecase').innerText = bot['usecase'];
    document.getElementById('bot_alert_message').value = bot['alarmMsg'];
    document.getElementById('bot_link_privacy').value = bot['privacyLink'];
    document.getElementById('bot_developer_name').value = bot['developer']['name'];
    document.getElementById('bot_developer_email').value = bot['developer']['email'];
    document.getElementById('bot_developer_address').value = bot['developer']['address'];
    document.getElementById('bot_developer_postcode').value = bot['developer']['postcode'];
    document.getElementById('bot_developer_city').value = bot['developer']['city'];
    document.getElementById('bot_developer_country').value = bot['developer']['country'];
    document.getElementById('bot_developer_website').value = bot['developer']['website'];
    document.getElementById('bot_developer_company').value = bot['developer']['company'];
}

function fieldsToBotIstance(){
    bot['name'] = document.getElementById('bot_name').value;
    bot['shortDescription'] = document.getElementById('bot_description').value;
    bot['longDescription'] = document.getElementById('bot_long_description').innerText;
    bot['licenceType'] = document.getElementById('bot_licence_type').value;
    bot['category'] = document.getElementById('bot_category').value;
    bot['classification'] =document.getElementById('bot_classification').value;
    bot['licenceFee'] = document.getElementById('bot_licence_fee').value;
    bot['version'] =  document.getElementById('bot_version').value;
    bot['usecase'] = document.getElementById('bot_usecase').innerText;
    bot['alarmMsg'] = document.getElementById('bot_alert_message').value;
    bot['privacyLink'] = document.getElementById('bot_link_privacy').value;
    bot['developer']['name'] = document.getElementById('bot_developer_name').value;
    bot['developer']['email'] = document.getElementById('bot_developer_email').value;
    bot['developer']['address'] = document.getElementById('bot_developer_address').value;
    bot['developer']['postcode'] = document.getElementById('bot_developer_postcode').value;
    bot['developer']['city'] = document.getElementById('bot_developer_city').value;
    bot['developer']['country'] = document.getElementById('bot_developer_country').value;
    bot['developer']['website'] = document.getElementById('bot_developer_website').value;
    bot['developer']['company'] = document.getElementById('bot_developer_company').value;
}

function setSelectValue(id,valueToSelect) {
    var element = document.getElementById(id);
    element.value = valueToSelect;
    element.selected = true;
    document.getElementById('select2-' + id + '-container').innerHTML = valueToSelect;
}

$(function () {
    $('.select2').select2();
});

$( document ).ready(function() {
    populateBotFields();
});