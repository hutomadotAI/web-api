document.getElementById("btnPublishRequest").addEventListener("click", checkInput);

$(function () {
    $('.select2').select2();
});

function checkInput(){
    // TODO remove hardcode example for next step to validation input
    var message = 'error occured';
    var alarm = 2;
    var id = 'ai_developer_company';

    createAlertMessage(alarm,message,id)
}

function createAlertMessage(alarm,message,id) {
    var msg_class;
    var ico_class;

    switch (alarm){
        case 0:
            msg_class = 'alert alert-dismissable flat alert-base';
            ico_class = 'icon fa fa-check';
            document.getElementById(id).style.border ="0px";
            break;
        case 1:
            msg_class = 'alert alert-dismissable flat alert-warning';
            ico_class = 'icon fa fa-check';
            document.getElementById(id).style.border ="1px solid orange";
            break;
        case 2:
            msg_class = 'alert alert-dismissable flat alert-danger';
            ico_class = 'icon fa fa-warning';
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

}

function populateBotFileds(){
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

function setSelectValue(id,valueToSelect) {
    var element = document.getElementById(id);
    element.value = valueToSelect;
    element.selected = true;
    document.getElementById('select2-' + id + '-container').innerHTML = valueToSelect;
}

$( document ).ready(function() {
    populateBotFileds();
});