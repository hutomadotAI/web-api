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
    $("#btnNext").attr("disabled",true);
    $("#btnCancel").attr("disabled",true);
    $('#btnNext').removeClass('btn btn-success flat').addClass('btn btn-success flat disabled');
    $('#btnCancel').removeClass('btn btn-primary flat').addClass('btn btn-primary flat disabled');

    document.getElementById("createAIform").submit();
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

var newNode = document.createElement('div');
newNode.className = 'row';
newNode.id = '_alert';

function createAlert(value){
    var wHTML = "";
    wHTML += ('<div class="box box-solid box-clean flat no-shadow" id="alert_message" style=" display: none;">');
    wHTML += ('<div class="box-body">');
    wHTML += ('<div class="col-xs-1">');
    wHTML += ('<h4><i class="fa fa-exclamation-circle text-md"></i></h4>');
    wHTML += ('</div>');
    wHTML += ('<div class="col-xs-8">');
    if( value == 1)
        wHTML += ('You need to update your profile');
    wHTML += ('</div>');
    wHTML += ('</div>');
    wHTML += ('</div>');
    newNode.innerHTML = wHTML;
    document.getElementById('newAicontent').appendChild(newNode);
}