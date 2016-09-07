document.getElementById("inputIntentName").addEventListener("keyup", activeButtonCreate);
document.getElementById("btnCreateIntent").addEventListener("click", PostingIntentName);


function activeButtonCreate() {
    var limitTextInputSize = 50;
    switch (limitText($("#inputIntentName"), limitTextInputSize)){
        case -1:
            $("#btnCreateIntent").prop("disabled",true);
            break;
        case 0:
            msgAlertIntent(0,'In this section you can create different intents.');
            $("#btnCreateIntent").prop("disabled", false);
            break;
        case 1:
            msgAlertIntent(1, 'Limit intent name reached!');
            $("#btnCreateIntent").prop("disabled", false);
            break;
        default:
            $("#btnCreateIntent").prop("disabled",true);
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


function createIntent() {
    $(this).prop("disabled",true);
    if(isContainInvalidCharacters($("#inputIntentName").val()))
        msgAlertIntent(2,'Intent name need contain only the following: A-Z, a-z, 0-9 character');
    else {
        PostingIntentName();
    }
}

function PostingIntentName(){
    $(this).prop("disabled",true);
    
    if(isContainInvalidCharacters($("#inputIntentName").val())) {
        msgAlertIntent(2, 'Intent name need contain only the following: A-Z, a-z, 0-9 character');
    }
    else {
        if(!document.intentCreateForm.onsubmit)
            return;
        document.intentCreateForm.submit();
    }
}

function isContainInvalidCharacters(txt) {
    var letters = /^[0-9a-zA-Z]+$/;
    if (letters.test(txt))
        return false;
    else
        return true;
}

function msgAlertIntent(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertIntent").attr('class','alert alert-dismissable flat alert-base');
            $("#icongAlertIntent").attr('class', 'icon fa fa-check');
            document.getElementById('inputIntentName').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertIntent").attr('class','alert alert-dismissable flat alert-warning');
            $("#icongAlertIntent").attr('class', 'icon fa fa-check');
            document.getElementById('inputIntentName').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertIntent").attr('class','alert alert-dismissable flat alert-danger');
            $("#icongAlertIntent").attr('class', 'icon fa fa-warning');
            document.getElementById('inputIntentName').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertIntent').innerText = msg;
}


function showIntents(str){
    var wHTML = "";

    if (intents.length < 1)
        msgAlertIntent(0,'No intents yet. Create the first one.');
    else
        msgAlertIntent(0,'In this section you can create different intents.');

    for (var x in intents) {
        if ( (str!=" ") && ( (str.length==0) || (intents[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {

            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');

            wHTML += ('<form method="POST" id="createIntentform" action="./editIntent.php" >');
            wHTML += ('<div class="col-xs-9" id="obj-entity">');
            wHTML += ('<div class="text-black" type="submit" id="entity-label'+x+'" onClick="editIntent(this.innerHTML)" onMouseOver="this.style.cursor=\'pointer\'">'+intents[x].name+'</div>')
            wHTML += ('</div>');
            wHTML += ('</form>');

            wHTML += ('<div class="col-xs-3" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray">');
            wHTML += ('<a data-toggle="dropdown">');
            wHTML += ('<i class="fa fa-cloud-download" style="padding-right: 5px;" data-toggle="tooltip" title="Download "></i>');
            wHTML += ('</a>');
            wHTML += ('<ul class="dropdown-menu flat">');
            wHTML += ('<li><a onClick="downloadIntent(\''+intents[x].name+'\','+x+',0)">JSON format</a></li>');
            wHTML += ('<li><a onClick="downloadIntent (\''+intents[x].name+'\','+x+',1)">CSV table</a></li>');
            wHTML += ('</ul>');
            wHTML += ('<a data-toggle="modal" data-target="#deleteIntent" id="'+x+'" style="cursor: pointer;">');
            wHTML += ('<i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i>');
            wHTML += ('</a>');
            wHTML += ('</div>');

            wHTML += ('</div>');
            wHTML += ('</div>');

            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</div>');
        }
    }

    newNode.innerHTML = wHTML;
    document.getElementById('intentsearch').appendChild(newNode);
}

function deleteIntent (elem) {
    delete intents[elem];
    showIntents('');
}

function OnMouseIn (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function editIntent(intent){
    document.getElementById("createIntentsform").submit();
}

function downloadIntent (name,value,flag) {
    name = name.replace(/[\|&;\$%@"<>\(\)\+,]/g, "");
    if( flag === 0){
        var blob = new Blob(["this file contains intent in JSON format"], { type: "text/plain;charset=utf-8;", });
       saveAs(blob, name+".txt");
    }
    else {
        var blob = new Blob(["this file is a simulaion of CVS format file"], { type: "text/plain;charset=utf-8;", });
        saveAs(blob, name+".csv");
    }
}

$('#deleteIntent').on('show.bs.modal', function(e) {
    var $modal = $(this), esseyId = e.relatedTarget.id;
    var elem = document.getElementById('delete-intent-label');
    var elemBtn = document.getElementById('modalDelete');
    var value = $('#intent-label'+esseyId).text();
    elem.innerHTML = 'Are you sure you would like to delete <label>' +  value +'</label> intent ? ';
    elemBtn.setAttribute("value", esseyId);
});
