document.getElementById("btnPublishRequest").addEventListener("click", checkInput);

document.getElementById("bot_name").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_description").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_longDescription").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_alertMessage").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_price").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_sample").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_privacyPolicy").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_version").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_videoLink").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_name").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_city").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_country").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_company").addEventListener("keydown",function(){removeAlert(this)}, false);
document.getElementById("bot_developer_email").addEventListener("keydown",function(){removeAlert(this)}, false);

populateDeveloperFields(developer);
populateBotFields(bot);

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
    
    // BOT licence fee ( price ) input validation
    var bot_price = document.getElementById('bot_price');
    if (bot_price.value != '' && bot_price.value !== 'undefined') {
        if (inputValidation(bot_price.value, 'bot_price')) {
            createAlertMessage(2, 'Please enter a valid number.','bot_price');
            return false;
        }
    }

    // block submit request
    $(this).prop("disabled", true);

    requestPublish();
    //uploadIconFile();
}

function requestPublish(){
    var prevCursor = document.body.style.cursor;
    document.body.style.cursor = 'wait';
    $("#btnPublishRequest").prop("disabled", true);

    if (!fieldsBotValidation()){
        $("#btnPublishRequest").prop("disabled", false);
        document.body.style.cursor = '';
        return false;
    }

    createAlertMessage(1, 'Sending request...');
    var botInfo = fieldsToBotIstance();
    var jsonString = JSON.stringify(botInfo);
    
    $.ajax({
        type: "POST",
        url: './dynamic/publishBot.php',
        data: {'bot' : jsonString},
        cache: false,
        success: function(response){
            var statusCode = JSON.parse(response);
            switch(statusCode['status']['code']){
                case 200:

                    createAlertMessage(3, 'Request sent!');
                    buttonPublishToRequest();
                    break;
                case 400:
                    createAlertMessage(2,'At least one of the required parameters is null or empty');
                    $("#btnPublishDeveloper").prop("disabled", false);
                    break;
                default:
                    createAlertMessage(2, response);
            }
        },
        complete: function () {
            document.body.style.cursor = prevCursor;
        },
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status + ' ' + thrownError);
            $("#btnPublishRequest").prop("disabled", false);
            createAlertMessage(2, 'Request not sent!');
        }
    });

}

function uploadIconFile() {

    var imageData = $('.image-editor').cropit('export');
    window.open(imageData);

    var formData = new FormData();
    formData.append("inputfile", document.getElementById('inputfile').files[0]);
    formData.append("tab","file");

    createAlertMessage(1,'Uploading image...');
    $.ajax({
        url : './dynamic/upload.php',
        type : 'POST',
        data : formData,
        dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: false,  // tell jQuery not to set contentType
        success: function (response) {
            var JSONdata = response;
            var statusCode = JSONdata['status']['code'];

            if (statusCode === 200) {
                var uploadWarnings = null;
                var additionalInfo = JSONdata['status']['additionalInfo'];

                if (additionalInfo != null)
                    uploadWarnings = getUploadWarnings(JSONdata['status']['additionalInfo']);

                if (uploadWarnings != null && uploadWarnings.length > 0)
                    msgAlertUploadFile(4, 'File uploaded, but with warnings:\n' + uploadWarnings.join("\n"));
                else
                    msgAlertUploadFile(4, 'File uploaded');
                setUICurrentStatus(1);
                hideRestartBox();
            } else {
                if (statusCode == 400 && haNoContentError(JSONdata['status']['additionalInfo'])) {
                    msgAlertUploadFile(2, 'File not uploaded. No right content was found.');
                } else {
                    msgAlertUploadFile(2, 'Something has gone wrong. File not uploaded.');
                }
                setUICurrentStatus(-1);
                disableButtonUploadTextFile(false);
                disableRestartBoxButton(false);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            createAlertMessage(2,'Unexpected error occurred during upload');
            $("#btnPublishRequest").prop("disabled", false);
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

function buttonPublishToRequest(){
    $('#btnPublishRequest').prop('disabled', true);
    document.getElementById('btnPublishRequest').className = 'btn btn-primary pull-right flat';
    document.getElementById('btnPublishRequestText').innerText = 'Request Sent';
    document.getElementById('iconPublishRequest').className = 'fa fa-check-circle';
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
        case 3:
            msg_class = 'alert alert-dismissable flat alert-success text-white';
            ico_class = 'icon fa fa-check';
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

function populateBotFields(bot){
    var json = JSON.parse(bot);
    document.getElementById('bot_id').value = json['botId'];
    document.getElementById('bot_aiid').value = json['aiid'];
    document.getElementById('bot_alertMessage').value = json['alertMessage'];
    document.getElementById('bot_badge').value = json['badge'];
    document.getElementById('bot_description').value = json['description'];
    document.getElementById('bot_longDescription').value = json['longDescription'];
    document.getElementById('bot_name').value = json['name'];
    document.getElementById('bot_price').value = json['price'];
    document.getElementById('bot_privacyPolicy').value = json['privacyPolicy'];
    document.getElementById('bot_sample').innerHTML = json['sample'];
    document.getElementById('bot_version').value = json['version'];
    document.getElementById('bot_videoLink').value = json['videoLink'];
    setSelectValue('bot_category',json['category']);
    setSelectValue('bot_classification',json['classification']);
    setSelectValue('bot_licenseType',json['licenseType']);
}

function populateDeveloperFields(developer) {
    var json = JSON.parse(developer);
    document.getElementById('bot_developer_address').value = json['address'];
    document.getElementById('bot_developer_city').value = json['city'];
    document.getElementById('bot_developer_company').value = json['company'];
    document.getElementById('bot_developer_country').value = json['country'];
    document.getElementById('bot_developer_email').value = json['email'];
    document.getElementById('bot_developer_name').value = json["name"];
    document.getElementById('bot_developer_postCode').value = json['postCode'];
    document.getElementById('bot_developer_website').value = json['website'];
}


function fieldsToBotIstance(){
    var bot={};
    bot['aiid'] = document.getElementById('bot_aiid').value;
    bot['botId'] = document.getElementById('bot_id').value;
    bot['alertMessage'] = document.getElementById('bot_alertMessage').value;
    bot['badge'] = document.getElementById('bot_badge').value;
    bot['category'] = getSelectValueText('bot_category')
    bot['classification'] = getSelectValueText('bot_classification')
    bot['description'] = document.getElementById('bot_description').value;
    bot['licenseType'] = getSelectValueText('bot_licenseType');
    bot['longDescription'] = document.getElementById('bot_longDescription').value;
    bot['name'] = document.getElementById('bot_name').value;
    bot['price'] = document.getElementById('bot_price').value;
    bot['privacyPolicy'] = document.getElementById('bot_privacyPolicy').value;
    bot['sample'] = document.getElementById('bot_sample').innerHTML;
    bot['version'] =  document.getElementById('bot_version').value;
    bot['videoLink'] = document.getElementById('bot_videoLink').value;
    return bot;
}

function getSelectValueText(id){
    var element = document.getElementById(id);
    var i = element.selectedIndex;
    return element.options[element.selectedIndex].text;
}

function setSelectValue(id,valueToSelect) {
    var element = document.getElementById(id);
    for (var i = 0; i < element.options.length; ++i) {
        if (element.options[i].text === valueToSelect) {
            element.options[i].selected = true;
            break;
        }
    }
}

function licenseTypeShow(){
    var myselect = document.getElementById('bot_licenseType');
    var val = parseInt(myselect.options[myselect.selectedIndex].value);

    switch(val){
        case 0:
            document.getElementById('collapseLicenseDetailsSubscription').className = 'panel-collapse collapse';
            document.getElementById('collapseLicenseDetailsPerpetual').className = 'panel-collapse collapse';
            break;
        case 1:
            document.getElementById('collapseLicenseDetailsSubscription').setAttribute('aria-expanded','true');
            document.getElementById('collapseLicenseDetailsSubscription').className = 'panel-collapse collapse in';
            document.getElementById('collapseLicenseDetailsPerpetual').className = 'panel-collapse collapse';
            break;
        case 2:
            document.getElementById('collapseLicenseDetailsPerpetual').setAttribute('aria-expanded','true');
            document.getElementById('collapseLicenseDetailsPerpetual').className = 'panel-collapse collapse in';
            document.getElementById('collapseLicenseDetailsSubscription').className = 'panel-collapse collapse';
            break;
    }
}

function fieldsBotValidation(){
    var elem;

    elem = document.getElementById('bot_name');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The name field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_description');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The description field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_longDescription');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The long description field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_licenseType');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The licenseType field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_category');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The category field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_classification');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The classification field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_sample');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The sample field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_price');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The price field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_alertMessage');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The alertMessage field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_version');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The version field cannot is empty!');
        return false;
    }

    elem = document.getElementById('bot_privacyPolicy');
    if (elem.value == '') {
        elem.style.border ="1px solid red";
        createAlertMessage(2, 'The privacy Policy field cannot is empty!');
        return false;
    }
    
    return true;
}

$(function () {
    $('.select2').select2();
});
