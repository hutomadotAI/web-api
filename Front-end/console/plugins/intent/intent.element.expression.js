document.getElementById("btnAddExpression").addEventListener("click", addUserExpression);

if (limitText($("#user-expression")) == 0)
    $("#btnAddExpression").prop("disabled", false);

function checkExpressionCode(element, key) {
    if (key == 13) {
        if( activeButtonCreateUserExpression())
            addUserExpression();
    }
    else {
        activeButtonCreateUserExpression();
    }
}

function activeButtonCreateUserExpression() {
    var limitTextInputSize = 50;
    switch (limitText($("#user-expression"), limitTextInputSize)) {
        case -1:
            $("#btnAddExpression").prop("disabled", true);
            return false;
        case 0:
            msgAlertUserExpression(0, ' Give the AI examples of how a user would express this intent.');
            $("#btnAddExpression").prop("disabled", false);
            return true;
        case 1:
            msgAlertUserExpression(1, 'The value user expression is too long!');
            $("#btnAddExpression").prop("disabled", true);
            return false
        default:
            $("#btnAddExpression").prop("disabled", true);
    }
    return false;
}

function addUserExpression() {
    $(this).prop("disabled", true);

    if (inputValidation($("#user-expression").val(), 'user_expression')) {
        msgAlertUserExpression(2, 'The user expression need contain only the following: BLA BLA BLA BLA character');
        return;
    }

    var expressions = [];
    var elements = document.getElementsByName('user-expression-row');
    for (var i = 0; i < elements.length; i++) {
        expressions.push(elements[i].value);
    }

    if(isNameExists($("#user-expression").val(),expressions)){
        msgAlertUserExpression(2, 'Two identical user expressions are not allowed. Please choose a different name.');
        return;
    }

    var element = document.getElementById('user-expression');
    var value = $(element).val();
    var parent = document.getElementById('userexpression-list');
    document.getElementById('user-expression').value = '';
    createNewUsersayRow(value, parent);
    msgAlertUserExpression(0,' Give the AI examples of how a user would express this intent.');
}

function createNewUsersayRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="expressionOnMouseIn (this)" onmouseout="expressionOnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-9" id="obj-userexpression">');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-comment-o text-gray"></i>');

    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="user-expression-row" name="user-expression-row" style="padding-left: 35px;background-color: #404446; " value="' + value + '" placeholder="' + value + '">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3" id="btnUserExpression" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');
    
    wHTML += ('<a data-toggle="modal" data-target="#deleteUserExpression" style="padding-right:3px; cursor: pointer;" onClick="deleteUserExpression(this)">');
    wHTML += ('<i class="fa fa-trash-o text-gray" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');
    
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);

    checkListExpressionSize();
}

function checkListExpressionSize() {
    if (document.getElementById('userexpression-list').childElementCount > 0)
        $("#btnAddExpression").prop("disabled", false);
    else
        $("#btnAddExpression").prop("disabled", true);
}

function deleteUserExpression(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    var elem =  $(parent.parentNode).find('input').attr('placeholder');
    parent.parentNode.removeChild(parent);
}

function expressionOnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function expressionOnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

$(document).ready(function () {
    if (typeof intent['user_says'] == "undefined" || !(intent['user_says'] instanceof Array))
        return;

    var list_expressions = intent['user_says'];
    for (var x in list_expressions) {
        var value = list_expressions[x];
        var parent = document.getElementById('userexpression-list');
        document.getElementById('user-expression').value = '';
        createNewUsersayRow(value, parent);
    }
});
