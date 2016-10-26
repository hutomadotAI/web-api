document.getElementById("btnAddExpression").addEventListener("click", addUserExpression);

function createNewUsersayRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body bg-white flat no-padding" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="expressionOnMouseIn (this)" onmouseout="expressionOnMouseOut (this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-9" id="obj-userexpression">');
    wHTML += ('<div class="inner-addon left-addon">');
    wHTML += ('<i class="fa fa-commenting-o text-gray"></i>');

    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="user-expression" name="user-expression" style="padding-left: 35px; " placeholder="' + value + '">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-3" id="btnUserExpression" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');
    wHTML += ('<a data-toggle="modal" data-target="#deleteUserExpression" style="cursor: pointer;" onClick="deleteUserExpression(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
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

    checkListExpressionIsEmpty();
}

function checkExpressionCode(element, key) {
    var value = $(element).val();
    document.getElementById('user-expression').style.borderColor = "#d2d6de";

    if (value.length > 0) {
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

function addUserExpression() {
    if (checkLimitExpression()) {
        var element = document.getElementById('user-expression');
        var value = $(element).val();
        var parent = document.getElementById('userexpression-list');
        document.getElementById('user-expression').value = '';
        createNewUsersayRow(value, parent);
    }
}

function deleteUserExpression(element) {
    // delete node from page - dipendence parentNode
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent)
    checkListExpressionIsEmpty();
    $("#btnSaveIntent").prop("disabled", false);
}

function checkListExpressionIsEmpty() {
    if (document.getElementById('userexpression-list').childElementCount > 0) {
        $("#btnSaveIntent").prop("disabled", false);
        $("#btnAddExpression").prop("disabled", false);
    }
    else {
        $("#btnSaveIntent").prop("disabled", true);
        $("#btnAddExpression").prop("disabled", true);
    }
}


function checkLimitExpression() {
    var limitTextInputSize = 50;
    switch (limitText($("#user-expression"), limitTextInputSize)) {
        case -1:
            return false;
        case 0:
            msgAlertUserExpression(0, 'You can add user expressions and save it!');
            return true;
        case 1:
            msgAlertUserExpression(1, 'Limit \'user says\' reached!');
            return false;
    }
}

function expressionOnMouseIn(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function expressionOnMouseOut(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}
