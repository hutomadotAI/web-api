function changeWebHookState(elem){
    var state = (elem.value == 'true');
    var node = $('#webhook');
    elem.innerText = ( state ) ? '  Deactive' : '  Active';
    state = !state;
    elem.value = state;
    node.attr('readonly', !node.attr('readonly'));
    msgAlertWebHook(ALERT.BASIC.value, 'Provide the WebHook endpoint');
}

$(document).ready(function () {
    if (typeof intent['webhook'] == "undefined")
        return;
    $('#webhook').val(intent['webhook']['endpoint']);
    if (intent['webhook']['enabled'])
        $('#btnWebHook').trigger("click")
});
