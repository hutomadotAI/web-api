function changeWebHookState(elem){
    var state = JSON.parse(elem.value);
    var node = $('#webhook');
    elem.innerText = ( state ) ? '  Deactive' : '  Active';
    state = !state;
    elem.value = state;
    node.attr('readonly', !node.attr('readonly'));
}

