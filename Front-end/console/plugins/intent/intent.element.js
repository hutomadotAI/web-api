$(document).ready(function() {
    // loading stored expressions
    for (var x in expressionListFromServer) {
        var value = expressionListFromServer[x].name;
        var parent = document.getElementById('userexpression-list');
        document.getElementById('user-expression').value = '';
        createNewUsersayRow(value,parent);
    }

    // loading stored parameter
    for (var x in parameterListFromServer) {
        var name = parameterListFromServer[x].name;
        var entity = '@'+parameterListFromServer[x].name;
        var value = parameterListFromServer[x].id;
        var parent = document.getElementById('parameter-list');

        createNewParameterRow(name,entity,value,parent);
    }

    // one parameter is empty for new input entry
    var node = document.getElementById('parameter-list');
    createNewParameterRow('','','',node);



    var $input = $('#action-entity');
    var array = [];

    // loading stored entities
    for (var x in entityListFromServer) {
        array.push('@'+entityListFromServer[x].name);
    }

    $input.omniselect({
        source: array,
        resultsClass: 'typeahead dropdown-menu flat no-padding no-border',
        activeClass: 'active',
        renderItem: function(label, id, index) {
            return '<li><a href="#">' + label + '</a></li>';
        }
    });

    $input.on('omniselect:select', function(event, value) {
        console.log('Selected: ' + value);
    });


});



(function() {
    var ga = document.createElement('script');
    ga.type = 'text/javascript';
    ga.async = true;
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
})();

checkListExpressionSize();

document.getElementById("addParameter").addEventListener("click", addNewParameter);
document.getElementById("btnAddExpression").addEventListener("click", addExpression);

$('#btnCreateIntent').bind('click', saveIntentElements );

//document.getElementById("btnCreateIntent").addEventListener("click", saveIntentElements);


function actionReaction(element,key){
    if(key == 13) {
        if (checkLimitAction()){
            var node = document.getElementById('parameter-list');
            // set focus after key 13 on Action
            var inputNode = node.children[0].children[1].children[0].children[0];
            inputNode.focus();
        }
    }
}


function addNewParameter(){
    var node = document.getElementById('parameter-list');
    createNewParameterRow('','','',node);
    
}

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

// delete node from page - dipendence parentNode
function deleteActionParameter (element) {
    // delete node from page - dipendence parentNode
    var parent =  (((element.parentNode).parentNode).parentNode).parentNode;
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

function OnMouseDeepIn (elem) {
    if ( elem != document.getElementById('parameter-list').children[0]) {
        var btn = elem.children[3].children[0].children[1].children[0];
        btn.style.display = '';
    }
}

function OnMouseDeepOut (elem) {
    if ( elem != document.getElementById('parameter-list').children[0]) {
        var btn = elem.children[3].children[0].children[1].children[0];
        btn.style.display = 'none';
    }
}


function checkKeyCode(element,key){
    var value = $(element).val();
    document.getElementById('user-expression').style.borderColor = "#d2d6de";

    if( value.length > 0) {
        document.getElementById('btnAddExpression').disabled = false;
        if (key == 13) {
            if (checkLimitExpression()) {
                document.getElementById('btnAddExpression').disabled = true;
                var parent = document.getElementById('userexpression-list');
                document.getElementById('user-expression').value = '';
                createNewUsersayRow(value, parent);
            }
        }
    }
    else {
        document.getElementById('btnAddExpression').disabled = true;
    }
}

function addExpression(){
    if (checkLimitExpression()){
        var element = document.getElementById('user-expression');
        var value = $(element).val();
        var parent = document.getElementById('userexpression-list');
        document.getElementById('user-expression').value = '';
        createNewUsersayRow(value,parent);
    }
}

function saveIntentElements () {
    alert("save data");
}

function createNewUsersayRow(value,parent){

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
    wHTML += ('<a data-toggle="modal" data-target="#deleteUserExpression" style="cursor: pointer;" onClick="deleteUserExpression(this)">');
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

    checkListExpressionSize();
}

function createNewParameterRow(name,entity,value,parent){
    

    if (typeof(name)==='undefined' || (name)=='') name = 'parameter name';
    if (typeof(entity)==='undefined' || (entity)=='') entity = 'entity name';
    if (typeof(value)==='undefined' || (value)=='') value = 'enter value';
  
    var wHTML ='';


    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="span3 form-control no-border" name="action-entity"  id="action-entity" style="margin: 0" placeholder="'+entity+'" autocomplete="off" >');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-4">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="form-control no-border" id="action-parameter" name="action-parameter"  placeholder="'+name+'" style="padding-left: 35px;">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3">');
    wHTML += ('<div class="text-center" >');
    wHTML += ('<input type="text" class="form-control no-border" id="action-value" name="action-value" placeholder="'+value+'" style="padding-left: 35px;">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2" style="padding-top:7px;">');
    wHTML += ('<div class="text-center" >');

    wHTML += ('<div class="col-xs-7 text-gray no-padding">');
    wHTML += ('<input class="pull-right" type="checkbox" id="required"> ');
    wHTML += ('</div>');
    wHTML += ('<div class="col-xs-5 text-gray no-padding">');
    wHTML += ('<a class="pull-right"  data-toggle="modal" data-target="#deleteActionParameter" style="display:none;" onClick="deleteActionParameter(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');
    wHTML += ('</div>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'box-body bg-white flat no-padding');
    newNode.setAttribute('onmouseover','OnMouseDeepIn (this)');
    newNode.setAttribute('onmouseout','OnMouseDeepOut (this)');

    newNode.style.border = '1px solid #d2d6de';
    newNode.style.marginTop ='-1px';
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    // be carefull - this is the treepath for input entity list
    var inputNode = newNode.children[0].children[0].children[0];

    var $input = $(inputNode);
    var array = [];

    // loading stored entities
    for (var x in entityListFromServer) {
        array.push('@'+entityListFromServer[x].name);
    }

    $input.omniselect({
        source: array,
        resultsClass: 'typeahead dropdown-menu flat no-padding no-border',
        activeClass: 'active',
        renderItem: function(label, id, index) {
            return '<li><a href="#">' + label + '</a></li>';
        }
    });

    $input.on('omniselect:select', function(event, value) {
        console.log('Selected: ' + value);
    });


}


function checkLimitExpression() {
    var limitTextInputSize = 50;
    switch (limitText($("#user-expression"), limitTextInputSize)){
        case -1:

            return false;
        case 0:
            msgAlertUserExpression(0, 'You can add user expressions and save it!');
            return true;
        case 1:
            msgAlertUserExpression(1, 'Limit \'user says\' reached!');

            return true;
    }
}

function checkLimitAction() {
    var limitTextInputSize = 20;
    switch (limitText($("#action-reaction"), limitTextInputSize)){
        case -1:

            return false;
        case 0:

            return true;
        case 1:
            msgAlertIntent(1, 'Limit \'Action \' reached!');

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
            document.getElementById('user-expression').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-check');
            document.getElementById('user-expression').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertUserExpression").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertUserExpression").attr('class', 'icon fa fa-warning');
            document.getElementById('user-expression').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertUserExpression').innerText = msg;
}


