/**
 * Created by Hutoma on 20/10/16.
 */
document.getElementById("btnAddIntentResponse").addEventListener("click", addIntentResponse);

function createNewIntentResponseRow(value,parent){
    var wHTML ='';

    wHTML += ('<div class="box-body bg-white flat no-padding" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="responseOnMouseIn (this)" onmouseout="responseOnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML +=('<div class="col-xs-9" id="obj-intentresponse">');
    wHTML +=('<div class="inner-addon left-addon">');
    wHTML +=('<i class="fa fa-commenting-o text-gray"></i>');

    wHTML +=('<input type="text" class="form-control flat no-shadow no-border" id="intent-response" name="intent-resposne" style="padding-left: 35px; " placeholder="'+value+'">');
    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML += ('<div class="col-xs-3" id="btnIntentResponse" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');
    wHTML += ('<a data-toggle="modal" data-target="#deleteIntentResponse" style="cursor: pointer;" onClick="deleteIntentResponse(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');
    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML +=('</div>');
    wHTML +=('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    checkListIntentResponseIsEmpty();
}

function checkIntentResponseCode(element,key){
    var value = $(element).val();
    document.getElementById('intent-response').style.borderColor = "#d2d6de";

    if( value.length > 0) {
        document.getElementById('btnAddIntentResponse').disabled = false;
        if (key == 13) {
            if (checkLimitIntentResponse()) {
                document.getElementById('btnAddIntentResponse').disabled = true;
                var parent = document.getElementById('intentresponse-list');
                document.getElementById('intent-response').value = '';
                createNewIntentResponseRow(value, parent);
            }
        }
    }
    else {
        document.getElementById('btnAddIntentResponse').disabled = true;
    }
}

function addIntentResponse(){
    if (checkLimitIntentResponse()){
        var element = document.getElementById('intent-response');
        var value = $(element).val();
        var parent = document.getElementById('intentresponse-list');
        document.getElementById('intent-response').value = '';
        createNewIntentResponseRow(value,parent);
    }
}

function deleteIntentResponse (element) {
    // delete node from page - dipendence parentNode
    var parent =  ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent)
    checkListIntentResponseIsEmpty();
}

function checkListIntentResponseIsEmpty(){
    if (document.getElementById('intentresponse-list').childElementCount > 0) {
        $("#btnSaveIntent").prop("disabled", false);
        $("#btnAddIntentResponse").prop("disabled", false);
    }
    else{
        $("#btnSaveIntent").prop("disabled",true);
        $("#btnAddIntentResponse").prop("disabled", true);
    }
}

function checkLimitIntentResponse() {
    var limitTextInputSize = 50;
    switch (limitText($("#intent-response"), limitTextInputSize)){
        case -1:
            return false;
        case 0:
            msgAlertIntentResponse(0, 'You can add response');
            return true;
        case 1:
            msgAlertIntentResponse(1, 'Limit \'response \' reached!');
            return false;
    }
}

function responseOnMouseIn (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function responseOnMouseOut (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}
