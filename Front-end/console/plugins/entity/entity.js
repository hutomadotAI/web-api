document.getElementById("inputEntityName").addEventListener("keyup", activeButtonCreate);
document.getElementById("btnCreateEntity").addEventListener("click", createEntity);


function activeButtonCreate() {
    var limitTextInputSize = 50;
    switch (limitText($("#inputEntityName"), limitTextInputSize)){
        case -1:
            $("#btnCreateEntity").prop("disabled",true);
            break;
        case 0:
            msgAlert(0,'In this section you can create different entitises.');
            $("#btnCreateEntity").prop("disabled", false);
            break;
        case 1:
            msgAlert(1, 'Limit entity name reached!');
            $("#btnCreateEntity").prop("disabled", false);
            break;
        default:
            $("#btnCreateEntity").prop("disabled",true);
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


function createEntity() {
    $(this).prop("disabled",true);
    if(isContainInvalidCharacters($("#inputEntityName").val()))
        msgAlert(2,'Entity name need contain only the following: A-Z, a-z, 0-9 character');
    else {
        CallBackendSaveEntity();
    }
}

function CallBackendSaveEntity(){
    if(!document.entityCreateForm.onsubmit){
        msgAlert(2,'An error is occured during submit data!');
        return;
    }
    //document.entityCreateForm.submit();
    alert("add entity to list");
}

function isContainInvalidCharacters(txt) {
    var letters = /^[0-9a-zA-Z]+$/;
    if (letters.test(txt))
        return false;
    else
        return true;
}

function msgAlert(alarm,msg){
    switch (alarm){
        case 0:
            $("#containerMsgAlertEntity").attr('class','alert alert-dismissable flat alert-base');
            $("#icongAlertEntity").attr('class', 'icon fa fa-check');
            document.getElementById('inputEntityName').style.borderColor = "#d2d6de";
            break;
        case 1:
            $("#containerMsgAlertEntity").attr('class','alert alert-dismissable flat alert-warning');
            $("#icongAlertEntity").attr('class', 'icon fa fa-check');
            document.getElementById('inputEntityName').style.borderColor = "orange";
            break;
        case 2:
            $("#containerMsgAlertEntity").attr('class','alert alert-dismissable flat alert-danger');
            $("#icongAlertEntity").attr('class', 'icon fa fa-warning');
            document.getElementById('inputEntityName').style.borderColor = "red";
            break
    }
    document.getElementById('msgAlertEntity').innerText = msg;
}


function showEntities(str){
    var wHTML = "";

    if (entities.length < 1)
        msgAlert(0,'No entities yet. Create the first one.');
    else
        msgAlert(0,'In this section you can create different entitises.');

    for (var x in entities) {
        if ( (str!=" ") && ( (str.length==0) || (entities[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {

            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');

            wHTML += ('<form method="POST" id="createEntityform" action="./editEntity.php" >');
            wHTML += ('<div class="col-xs-9" id="obj-entity">');
            wHTML += ('<div class="text-black" type="submit" id="entity-label'+x+'" onMouseOver="this.style.cursor=\'pointer\'">@'+entities[x].name+'</div>')
            wHTML += ('</div>');
            wHTML += ('</form>');

            wHTML += ('<div class="col-xs-3" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray">');
            wHTML += ('<a data-toggle="dropdown">');
            wHTML += ('<i class="fa fa-cloud-download" style="padding-right: 5px;" data-toggle="tooltip" title="Download "></i>');
            wHTML += ('</a>');
            wHTML += ('<ul class="dropdown-menu flat">');
            wHTML += ('<li><a onClick="downloadEntity (\''+entities[x].name+'\','+x+',0)">JSON format</a></li>');
            wHTML += ('<li><a onClick="downloadEntity (\''+entities[x].name+'\','+x+',1)">CSV table</a></li>');
            wHTML += ('</ul>');
            wHTML += ('<a data-toggle="modal" data-target="#deleteEntity" id="'+x+'" style="cursor: pointer;">');
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
    document.getElementById('entsearch').appendChild(newNode);
}

function deleteEntity (elem) {
    delete entities[elem];
    showEntities('');
}

function OnMouseIn (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}

function OnMouseOut (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}

function downloadEntity (name,value,flag) {
    name = name.replace(/[\|&;\$%@"<>\(\)\+,]/g, "");
    if( flag === 0){
        var blob = new Blob(["this file contains entity in JSON format"], { type: "text/plain;charset=utf-8;", });
       saveAs(blob, name+".txt");
    }
    else {
        var blob = new Blob(["this file is a simulaion of CVS format file"], { type: "text/plain;charset=utf-8;", });
        saveAs(blob, name+".csv");
    }
}

$('#deleteEntity').on('show.bs.modal', function(e) {
    var $modal = $(this), esseyId = e.relatedTarget.id;
    var elem = document.getElementById('delete-entity-label');
    var elemBtn = document.getElementById('modalDelete');
    var value = $('#entity-label'+esseyId).text();
    elem.innerHTML = 'Are you sure you would like to delete <label>' +  value +'</label> entity ? ';
    elemBtn.setAttribute("value", esseyId);
});