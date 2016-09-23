document.getElementById("btnNext").addEventListener("click", wizardNext);
document.getElementById("btnCancel").addEventListener("click", clearInputFields);
document.getElementById("ai_name").addEventListener("keyup", activeBtnNext);

var ai_name = $("#ai_name").val();
if ( ai_name.length > 0 )
    $("#btnNext").prop("disabled",false);
else
    $("#btnNext").prop("disabled",true);

function wizardNext() {
    $(this).prop("disabled",true);
    $("#btnCancel").prop("disabled",true);

    if(isContainInvalidCharacters($("#ai_name").val())) {
        msgAlert(2, 'Ai name need contain only the following: A-Z, a-z, 0-9 character');
        inputsActiveDeactive(false);
        return;
    }

    if(document.createAIform.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.createAIform.submit();
}

function activeBtnNext() {
    var ai_name = $("#ai_name").val();
    if ( ai_name.length > 0 ) {
        $("#btnNext").prop("disabled", false);
        document.getElementById('containerMsgAlertNameAI').style.display = 'none';
        document.getElementById('ai_name').style.borderColor = "#d2d6de";
    }
    else
        $("#btnNext").prop("disabled",true);
}

function inputsActiveDeactive(flag){
    $("#btnNext").prop("disabled",!flag);
    $("#btnCancel").prop("disabled",flag);
    $("#ai_name").prop("disabled",flag);
    $("#ai_description").prop("disabled",flag);
    $("#ai_confidence").prop("disabled",flag);
    $("#ai_timezone").prop("disabled",flag);
    $("#ai_sex").prop("disabled",flag);
    $("#ai_language").prop("disabled",flag);
    $("#ai_personality").prop("disabled",flag);
}

function isContainInvalidCharacters(txt) {
    var letters = /^[0-9a-zA-Z]+$/;
    if (letters.test(txt))
        return false;
    else
        return true;
}

function clearInputFields() {
    $("#btnNext").prop("disabled",true);
    document.getElementById('containerMsgAlertNameAI').style.display = 'none';
    $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-base');
    $("#icongAlertNameAI").attr('class', 'icon fa fa-check');

    document.getElementById('ai_name').style.borderColor = "#d2d6de";
    document.getElementById('ai_name').value = '';

    document.getElementById("ai_sex")[0].selected = true;
    document.getElementById("ai_sex")[1].selected = false;
    document.getElementById("ai_sex")[0].value = ("Male");
}

function msgAlert(alarm,msg){
    document.getElementById('containerMsgAlertNameAI').style.display = 'block';
    switch (alarm){
        case 0:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-base');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_name').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-warning');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-check');
            document.getElementById('ai_name').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertNameAI").attr('class','alert alert-dismissable flat alert-danger');
            $("#iconAlertNameAI").attr('class', 'icon fa fa-warning');
            document.getElementById('ai_name').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertNameAI').innerText = msg;
}
    
$(function () {
    $(".select2").select2();
});

$(function () {
    $('.slider').slider();
    $("#ai_confidence").ionRangeSlider({
        type: "single",
        min: 1,
        max: 4,
        from:2,
        from_value:"sometimes",
        step: 1,
        grid: true,
        keyboard: true,
        onStart: function (data) {console.log("onStart"); },
        onChange: function (data) {console.log("onChange"); },
        onFinish: function (data) { console.log("onFinish"); },
        onUpdate: function (data) {console.log("onUpdate"); },
        values: ["never", "sometimes", "often","always"]
    });
});

//iCheck for checkbox and radio inputs
$('input[type="checkbox"].minimal, input[type="radio"].minimal').iCheck({
    checkboxClass: 'icheckbox_minimal-blue',
    radioClass: 'iradio_minimal-blue'
});