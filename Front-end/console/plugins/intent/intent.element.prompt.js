function createNewPromptRow(value, parent) {
    var wHTML = '';

    wHTML += ('<div class="box-body flat no-padding" style="background-color: #404446; border: 1px solid #202020; margin-top: -1px;" onmouseover="promptOnMouseInRow(this)" onmouseout="promptOnMouseOutRow(this)">');
    wHTML += ('<div class="row">');

    wHTML += ('<div class="col-xs-10" id="obj-prompt">');
    wHTML += ('<div class="inner-addon left-addon" style="background-color: #404446;">');
    wHTML += ('<i class="fa fa-tag text-gray"></i>');
    
    wHTML += ('<input type="text" class="form-control flat no-shadow no-border" id="row-prompt" name="row-prompt"  style="background-color: #404446;" placeholder="' + value + '">');
    wHTML += ('</div>');
    wHTML += ('</div>');

    wHTML += ('<div class="col-xs-2" id="btnRowPrompt" style="display:none;" >');
    wHTML += ('<div class="btn-group pull-right text-gray" style="padding-right:7px; padding-top:7px;">');

    wHTML += ('<a data-toggle="modal" data-target="#deleteRowPrompt" style="padding-right:3px;" onClick="deleteRowPrompt(this)">');
    wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
    wHTML += ('</a>');

    wHTML += ('</div>');
    wHTML += ('</div>');

    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12 no-padding');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.firstChild);
}

function deleteRowPrompt(element) {
    var parent = ((((element.parentNode).parentNode).parentNode).parentNode).parentNode;
    parent.parentNode.removeChild(parent);
}

function promptOnMouseInRow(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function promptOnMouseOutRow(elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function checkInputPromptCode(element, key) {
    if (key == 13) {
        if (checkLimitPrompt()) {
            var value = $(element).val();
            var parent = document.getElementById('prompts-list');
            document.getElementById('input-prompt').value = '';
            createNewPromptRow(value, parent);
            removeAlertMsgPrompt();
        }
    }
}

function checkLimitPrompt() {
    var limitTextInputSize = 50;
    switch (limitText($("#input-prompt"), limitTextInputSize)) {
        case -1:
            return false;
        case 0:
            return true;
        case 1:
            var node = document.getElementById('alertMsgPrompt');
            var wHTML = '';
            wHTML += ('<div class="alert alert-dismissable flat alert-danger" id="containerMsgAlertPrompt">');
            wHTML += ('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>');
            wHTML += ('<i class="icon fa fa-warning" id="icongAlertPrompt"></i>');
            wHTML += ('<span id="msgAlertPrompt">Text prompt is too long!</span>');
            wHTML += ('</div>');
            node.innerHTML = wHTML;
            return false;
    }
}

function removeAlertMsgPrompt() {
    var node = document.getElementById('alertMsgPrompt');
    node.innerHTML = '';
}
