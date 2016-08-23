var name = $("#ai_name").val();

if ( name.length > 0 ){
    $("#btnNext").removeClass("btn btn-success flat disabled").addClass("btn btn-success flat");
    $("#btnNext").attr("value","_next");
    $("#btnNext").attr("onClick","submitForm()");
}

function activeNext(str) {
  var name = $("#ai_name").val();
if ( name.length > 0 ){
    $("#btnNext").removeClass("btn btn-success flat disabled").addClass("btn btn-success flat");
    $("#btnNext").attr("value","_next");
    $("#btnNext").attr("onClick","submitForm()");
}
else{
    $('#btnNext').removeClass('btn btn-success flat').addClass('btn btn-success flat disabled');
    $("#btnNext").attr("value","");
    $("#btnNext").attr("onClick","");
}
}

function submitForm() {

    RecursiveUnbind($('#wrapper'));

    $("#btnNext").attr("disabled",true);
    $("#btnCancel").attr("disabled",true);
    $('#btnNext').removeClass('btn btn-success flat').addClass('btn btn-success flat disabled');
    $('#btnCancel').removeClass('btn btn-primary flat').addClass('btn btn-primary flat disabled');
    document.getElementById("createAIform").submit();
}

function showInfoMessage(){

   /* var wHTML='';
    wHTML += ('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>');
    wHTML += ('<i class="icon fa fa-check"></i>');
    wHTML += ('<span> Before go on you fill the fi</span>');
    
    var newNode = document.createElement('div');
    newNode.className = 'alert alert-base alert-dismissable flat';
    newNode.id = 'inputNameMsgAlert';
    newNode.innerHTML = wHTML;
    document.getElementById('ai_name_alert').appendChild(newNode);
    */
    
}

$(function () {
    $(".select2").select2();
});

$(function () {
    $('.slider').slider();
    $("#confidence").ionRangeSlider({
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