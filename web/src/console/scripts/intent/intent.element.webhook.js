function changeWebHookState(elem){
    var state = (elem.value == 'true');
    var node = $('#webhook');
    elem.innerText = ( state ) ? '  Deactive' : '  Active';
    state = !state;
    elem.value = state;
    node.attr('readonly', !node.attr('readonly'));
    msgAlertWebHook(ALERT.BASIC.value, 'Give the bot the webhook endpoint.');
}

$(document).ready(function () {
    if (typeof intent['webhook'] == "undefined")
        return;
    $('#webhook').val(intent['webhook']['endpoint']);
    if (intent['webhook']['enabled'])
        $('#btnWebHook').trigger("click")
});
