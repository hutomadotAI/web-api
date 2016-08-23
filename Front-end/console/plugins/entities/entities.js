function showEntities(str){
    var wHTML = "";
    wHTML += ('<div class="box-body">');

    for (var x in entities) {
        if ( (str!=" ") && ( (str.length==0) || (entities[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {

            wHTML += ('<div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');


            wHTML += ('<div class="col-xs-9" id="obj-entity">');
            wHTML += ('<input type="text" class="form-control no-border no-padding" name="entity-label" id="entity-label" placeholder="'+entities[x].name+'" onkeyup="activeSave(this.value,'+x+')">')
            wHTML += ('</div>');

            wHTML += ('<div class="col-xs-3" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray" style="padding-top: 5px;">');
            wHTML += ('<a data-toggle="control-sidebar" onClick="saveEntity ('+x+')"><i class="fa fa-cloud-download" style="padding-right: 5px;"></i></a>');
            wHTML += ('<a data-toggle="control-sidebar" onClick="deleteEntity ('+x+')"><i class="fa fa-trash-o" hidden></i></a>');
            wHTML += ('</div>');
            wHTML += ('</div>');

            wHTML += ('</div>');

            wHTML += ('</div>');
        }
    }
    wHTML += ('</div>');
    newNode.innerHTML = wHTML;
    document.getElementById('entsearch').appendChild(newNode);
}

function deleteEntity (elem) {
    delete entities[elem];
    showEntities('');
}
function saveEntity (elem) {
    //entities[elem];
}
function OnMouseIn (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = '';
}
function OnMouseOut (elem) {
    var btn = elem.children[0].children[1];
    btn.style.display = 'none';
}
function activeSave(label,value) {
    if ( label.length > 0 )
        saveEntity(value)

}
