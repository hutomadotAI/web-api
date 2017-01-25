function passwordStrength(password) {

    var desc = [{'width': '20%'}, {'width': '40%'}, {'width': '60%'}, {'width': '80%'}, {'width': '100%'}];
    var descClass = ['progress-bar-danger', 'progress-bar-danger', 'progress-bar-warning', 'progress-bar-success', 'progress-bar-success'];
    var descText = ['weak', 'weak', 'medium', "strong", "very strong"];
    var fullScore = 6;
    var score = 0;

    if ( $("#passwordField").val().length == 0 )
        $("#progress_strength").css("visibility", "hidden");
    else
        $("#progress_strength").css("visibility", "visible");


    for (var i = 0; i < descClass.length; i++) {
        $("#pstrength").removeClass(descClass[i]);
    }

    if (password == '') {
        $("#pstrength").addClass('progress-bar-danger').css({'width': '0px'});
        $("#progress-bar-text").innerHTML = '';
    } else {

        //if password bigger than 6 give 1 point
        if (password.length > 6) score++;

        //if password has both lower and uppercase characters give 1 point
        if ((password.match(/[a-z]/)) && (password.match(/[A-Z]/))) score++;

        //if password has at least one number give 1 point
        if (password.match(/d+/)) score++;

        //if password has at least one special caracther give 1 point
        if (password.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/))    score++;

        //if password bigger than 12 give another 1 point
        if (password.length > 10) score++;

        var pos = Math.round((score * desc.length) / fullScore);
        if (pos >= desc.length)  pos = desc.length - 1;
        $("#pstrength").addClass(descClass[pos]).css(desc[pos]);
        $("#progress-bar-text").html(descText[pos]);
    }

}

function confirmPassword(passFieldId, confirmationFieldId) {
    var pwd1 = $("#" + passFieldId).val();
    var pwd2 = $("#" + confirmationFieldId).val();
    if (pwd1 != pwd2) {
        $("#" + confirmationFieldId).addClass("form-control-error");
    } else {
        $("#" + confirmationFieldId).removeClass("form-control-error");
    }
}