checkListExpressionSize();

function checkListExpressionSize(){
    if (document.getElementById('userexpression-list').childElementCount > 0)
        $("#btnSaveIntent").prop("disabled",false);
    else
        $("#btnSaveIntent").prop("disabled",true);
}

function deleteUserExpression (element) {
    // delete node from page - dipendence parentNode
    var parent =  ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent)
    checkListExpressionSize();
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
        if (checkLimitUsersay()){
            var value = $(element).val();
            var parent = document.getElementById('userexpression-list');
            document.getElementById('user-expression').value = '';
            createNewUsersayRow(element,value,parent);
        }
    }
}


function findEntityList(element){
    var prefix = $(element).val();
    loadEntitySublist(element,prefix);
}

function loadEntitySublist(element,str){
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'entity_list';

    var wHTML = '';

    //wHTML += ('<div class="box-tools pull-right">');
   // wHTML += ('<ul class="dropdown-menu flat">');
    for (var x in entityList) {
        if ( (str!=" ") && ( (str.length==0) || (entityList[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {
            if(entityList.length!=0){
                wHTML += ('<li class="footer" onMouseOver="this.style.cursor=\'pointer\'">'+entityList[x].name+'</li>');
            }
        }
    }
   // wHTML += ('</ul>');
    //wHTML += ('</div>');

    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    newNode.innerHTML = wHTML;

    document.getElementById('btnList').click();
    element.focus();
    parent.appendChild(newNode);
}

function createNewUsersayRow(elem,value,parent){

    var wHTML ='';

    wHTML += ('<div class="box-body bg-white flat no-padding" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML +=('<div class="col-xs-9" id="obj-userexpression">');
    wHTML +=('<div class="inner-addon left-addon">');
    wHTML +=('<i class="fa fa-commenting-o text-gray"></i>');

    wHTML +=('<input type="text" class="form-control no-border" id="user-expression" name="user-expression" style="padding-left: 35px; " placeholder="'+value+'">');
    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML += ('<div class="col-xs-3" id="btnUserExpression" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');
    wHTML += ('<a data-toggle="modal" data-target="#deleteUserExpression" id="x" style="cursor: pointer;" onClick="deleteUserExpression(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');
    wHTML +=('</div>');
    wHTML +=('</div>');

    wHTML +=('</div>');
    wHTML +=('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    checkListExpressionSize();
}


function checkLimitUsersay() {
    var limitTextInputSize = 50;
    switch (limitText($("#user-expression"), limitTextInputSize)){
        case -1:
            $("#btnSaveIntent").prop("disabled",true);
            return false;
        case 0:
            $("#btnSaveIntent").prop("disabled", true);
            return true;
        case 1:
            msgAlertIntent(1, 'Limit \'user says\' reached!');
            $("#btnSaveIntent").prop("disabled", false);
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

function doOn(obj)
{
    if(obj.id=="mydef")
    {
        document.getElementById("def1").style.display="none";
        document.getElementById("def").style.display="block";
    }
    if(obj.id=="search")
    {
        document.getElementById("def").style.display="none";

        document.getElementById("def1").innerHTML='<li><a id="Java" onclick="mydef(this);" >java</a></li><li><a id="oracle" onclick="mydef(this);" >Oracle</a></li>';

        document.getElementById("def1").style.display="block";
    }

}

function mydef(obj)
{
    document.getElementById("search").value=obj.innerHTML;
    document.getElementById("def1").style.display="none";
    document.getElementById("def").style.display="none";
}

