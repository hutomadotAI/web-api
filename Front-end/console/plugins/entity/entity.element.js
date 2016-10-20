checkListEntityValuesSize();

function  createNewValueEntityRow(value,parent){

    var wHTML ='';

    wHTML +=('<div class="box-body bg-white flat no-padding" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
    wHTML +=('<div class="row">');

    wHTML +=('<div class="col-xs-9" id="obj-value-entity">');
    wHTML +=('<div class="inner-addon left-addon">');
    wHTML +=('<i class="fa fa-language text-gray"></i>');

    wHTML +=('<input type="text" class="form-control flat no-shadow no-border" id="value-entity" name="value-entity" style="padding-left: 35px; " placeholder="'+value+'">');
    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML +=('<div class="col-xs-3" id="btnValueEntity" style="display:none;" >');
    wHTML +=('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');

    wHTML +=('<a data-toggle="modal" data-target="#deleteValueEntity" style="padding-right:3px;" onClick="deleteValueEntity(this)">');
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

    checkListEntityValuesSize();
}

function checkListEntityValuesSize(){
    if (document.getElementById('entityValues-list').childElementCount > 0)
        $("#btnSaveEntity").prop("disabled",false);
    else
        $("#btnSaveEntity").prop("disabled",true);
}

function OnMouseIn (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function checkValueCode(element,key){
    if(key == 13) {
        if (checkLimitValue()){
            var value = $(element).val();
            var parent = document.getElementById('entityValues-list');
            document.getElementById('value-entity').value = '';
            createNewValueEntityRow(value,parent);
        }
    }
}

function checkLimitValue() {
    var limitTextInputSize = 50;
    switch (limitText($("#value-entity"), limitTextInputSize)){
        case -1:
            return false;
        case 0:
            return true;
        case 1:
            msgAlertEntityValue(1, 'The value is too long!');
            return false;
    }
}


function deleteValueEntity (element) {
    // delete node from page - dipendence parentNode
    var parent =  ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent)
    checkListEntityValuesSize();

}

$(document).ready(function() {
    // loading stored entities values
    for (var x in entityValuesListFromServer) {
        var value = entityValuesListFromServer[x];
        var parent = document.getElementById('entityValues-list');
        document.getElementById('value-entity').value = '';
        createNewValueEntityRow(value,parent);
    }
});
