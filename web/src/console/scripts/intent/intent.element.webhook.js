$(document).ready(function () {
    if (typeof intent['webhook'] == "undefined")
        return;
    $('#webhook').val(intent['webhook']['endpoint']);
});

function updateWebhookSaving(){
    enableSaving(true);
    msgAlertWebHook(ALERT.BASIC.value, 'Provide the WebHook endpoint.');
}