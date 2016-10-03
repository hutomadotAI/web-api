document.getElementById('btnNext').addEventListener('click', wizardNext);
document.getElementById('ai_name').addEventListener('keyup', activeBtnNext);

function wizardNext() {
    $(this).prop('disabled',true);
    $('#btnCancel').prop('disabled',true);

    if(isContainInvalidCharacters($('#ai_name').val())) {
        msgAlertNameAI(2, 'Ai name need contain only the following: A-Z, a-z, 0-9 _ or -');
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
        $('#btnNext').prop('disabled', false);
        document.getElementById('containerMsgAlertNameAI').style.display = 'none';
        document.getElementById('ai_name').style.borderColor = "#d2d6de";
    }
    else
        $("#btnNext").prop("disabled",true);
}

function inputsActiveDeactive(flag){
    $('#btnNext').prop('disabled',!flag);
    $('#btnCancel').prop('disabled',flag);
    $('#ai_name').prop('disabled',flag);
    $('#ai_description').prop('disabled',flag);
    $('#ai_confidence').prop('disabled',flag);
    $('#ai_timezone').prop('disabled',flag);
    $('#ai_sex').prop('disabled',flag);
    $('#ai_language').prop('disabled',flag);
    $('#ai_personality').prop('disabled',flag);
}

function isContainInvalidCharacters(txt) {
    var letters = /^[0-9a-zA-Z \-'_]+$/;
    if (letters.test(txt))
        return false;
    return true;
}

    
$(function () {
    $('.select2').select2();
});

$(function () {
    
    $('#ai_confidence').ionRangeSlider({
        type: "single",
        min: 1,
        max: 4,
        from:2,
        from_value:"sometimes",
        step: 1,
        grid: true,
        keyboard: true,
        onStart: function (data) {console.log('onStart'); },
        onChange: function (data) {console.log('onChange'); },
        onFinish: function (data) { console.log('onFinish'); },
        onUpdate: function (data) {console.log('onUpdate'); },
        values: ["never", "sometimes", "often","always"]
    });
});


//Flat red color scheme for iCheck
$('input[type="checkbox"].flat-red, input[type="radio"].flat-red').iCheck({
    checkboxClass: 'icheckbox_flat-blue'
});


$(document).ready(function(){

    if ( previousFilled == 1 ){
        fillInputFields();
        // active button next if are previous inserted data
        if (  $('#ai_name').val().length > 0 )
            $('#btnNext').prop('disabled', false);
        else
            $('#btnNext').prop('disabled',true);
    }
});

function selectInputElement(id,valueToSelect) {
    var element = document.getElementById(id);
    element.value = valueToSelect;
    element.selected = true;
    document.getElementById('select2-' + id + '-container').innerHTML = valueToSelect;
}

function fillInputFields(){
    document.getElementById('ai_name').value = previousField.name;
    document.getElementById('ai_description').value = previousField.description;

    selectInputElement('ai_language',previousField.language);
    selectInputElement('ai_timezone',previousField.timezone);
    selectInputElement('ai_voice',previousField.voice);
    selectInputElement('ai_personality',previousField.personality);

    document.getElementById('ai_confidence').value = previousField.confidence;
    $("#ai_confidence").ionRangeSlider('upload');

    // enable/disable checkbox public AI

    if(previousField.private == '1') {
        $('input[type="checkbox"].flat-red').prop('checked',false)
    }
    else {
        $('input[type="checkbox"].flat-red').prop('checked', true);
    }

}