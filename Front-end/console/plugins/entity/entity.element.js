$(document).ready(function() {
    // loading stored entities keys
    for (var x in entityKeysListFromServer) {
        var value = entityKeysListFromServer[x].name;
        var parent = document.getElementById('entityKeys-list');
        document.getElementById('key-entity').value = '';
        createNewKeyEntityRow(value,parent);
    }
});

checkListEntityKeysSize();

function checkListEntityKeysSize(){
    if (document.getElementById('entityKeys-list').childElementCount > 0)
        $("#btnSaveEntity").prop("disabled",false);
    else
        $("#btnSaveEntity").prop("disabled",true);
}

function deleteKeyEntity (element) {
    // delete node from page - dipendence parentNode

    var parent =  ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent)
    checkListEntityKeysSize();

}

function OnMouseIn (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function checkKeyCode(element,key){
    if(key == 13) {
        if (checkLimitKey()){
            var value = $(element).val();
            var parent = document.getElementById('entityKeys-list');
            document.getElementById('key-entity').value = '';
            createNewKeyEntityRow(value,parent);
        }
    }
}

function checkPromptCode(element,key){
    if(key == 13) {
        if (checkLimitPrompt()){
            var value = $(element).val();
            document.getElementById('btnSaveEntity').ocus();
        }
    }
}
function saveEntityElements () {



}

function  createNewKeyEntityRow(value,parent){

    var wHTML ='';

    wHTML +=('<div class="box-body bg-white flat no-padding" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
    wHTML +=('<div class="row">');

    wHTML +=('<div class="col-xs-9" id="obj-keyentity">');
    wHTML +=('<div class="inner-addon left-addon">');
    wHTML +=('<i class="fa fa-language text-gray"></i>');

    wHTML +=('<input type="text" class="form-control no-border" id="key-entity" name="key-entity" style="padding-left: 35px; " placeholder="'+value+'">');
    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML +=('<div class="col-xs-3" id="btnKeyEntity" style="display:none;" >');
    wHTML +=('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');

    wHTML +=('<a style="padding-right:7px;">');
    wHTML +=('<i class="glyphicon glyphicon-erase text-sm text-yellow" data-toggle="tooltip" title="This parametere needs to" ></i>');
    wHTML +=('</a>');

    wHTML +=('<a data-toggle="modal" data-target="#deleteKeyEntity" style="padding-right:3px;" onClick="deleteKeyEntity(this)">');
    wHTML +=('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML +=('</a>');

    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML +=('</div>');
    wHTML +=('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    checkListEntityKeysSize();
}



function checkLimitKey() {
    var limitTextInputSize = 50;
    switch (limitText($("#key-entity"), limitTextInputSize)){
        case -1:

            return false;
        case 0:

            return true;
        case 1:
            msgAlertIntent(1, 'Limit \'entity keys\' reached!');

            return true;
    }
}

function checkLimitPrompt() {
    var limitTextInputSize = 30;
    switch (limitText($("#key-entity"), limitTextInputSize)){
        case -1:

            return false;
        case 0:

            return true;
        case 1:
            msgAlertIntent(1, 'Limit \'entity prompt\' reached!');

            return true;
    }
}

function limitText(limitField, limitNum) {
    if (limitField.val().length < 1)
        return -1;
    if (limitField.val().length >= limitNum) {
        limitField.val(limitField.val().substring(0, limitNum));
        return 1;
    }
    return 0;
}

function msgAlertUserExpression(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-check');
            document.getElementById('inputUserExpression').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-check');
            document.getElementById('inputUserExpression').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-warning');
            document.getElementById('inputUserExpression').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertUserExpression').innerText = msg;
}
