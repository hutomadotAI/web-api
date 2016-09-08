document.getElementById("btnSave").addEventListener("click", wizardNext);
document.getElementById("btnSkip").addEventListener("click", wizardSkip);
document.getElementById("btnBack").addEventListener("click", backPage);
document.getElementById("ai_price").addEventListener("keyup", checkValue);


function wizardNext() {
    $("#btnSave").prop("disabled",true);
    $("#btnCancel").prop("disabled",true);

    if(isContainInvalidCharacters($("#ai_price").val())) {
        msgAlert(2, 'The price needs contains only a price format. Please insert the correct value');
        return;
    }

    if(document.marketNewAIform.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.marketNewAIform.submit();
}

function wizardSkip() {
    $("#btnSave").prop("disabled",true);
    $("#btnCancel").prop("disabled",true);

    if(document.marketNewAIform.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.marketNewAIform.submit();
}

function backPage(){
    $(this).prop("disabled",true);
    history.go(-1);
    return false;
}

function checkValue() {
    $("#btnSave").prop("disabled",false);
    $("#btnSkip").prop("disabled",false);
    $("#btnCancel").prop("disabled",false);
    if(document.getElementById('containerMsgAlertPriceAI').getAttribute("style")!=null && document.getElementById('containerMsgAlertPriceAI').getAttribute("style")!="") {
        document.getElementById('containerMsgAlertPriceAI').style.display = 'none';
    }
    document.getElementById('ai_price').style.borderColor = "#d2d6de";
}

function isContainInvalidCharacters(price) {
    var regex = /^[1-9]\d*(((,\d{3}){1})?(\.\d{0,2})?)$/;
    if (regex.test(price))
        return false;
    else
        return true;
}

function msgAlert(alarm,msg){
    document.getElementById('containerMsgAlertPriceAI').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertPriceAI").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertPriceAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_price').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertPriceAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertPriceAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_price').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertPriceAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertPriceAI").attr('class', 'icon fa fa-warning');
            document.getElementById('ai_price').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertPriceAI').innerText = msg;
}

