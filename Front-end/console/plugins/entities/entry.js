function showEntities(str){
    var wHTML = "";


    for (var x in entities) {
        if ( (str!=" ") && ( (str.length==0) || (entities[x].name.toLowerCase()).indexOf(str.toLowerCase())!=-1 ) )  {

            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body bg-white flat" style=" border: 1px solid #d2d6de; margin-top: -1px;" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');


            wHTML += ('<div class="col-xs-3" id="obj-entity">');
            wHTML += ('<input type="text" class="form-control no-border no-padding" name="entity-label" id="entity-label" placeholder="'+entities[x].name+'"  onkeydown="if(event.keyCode == 13 ){ changeFocus(); }" onkeyup="activeSave(this.value,'+x+')">')
            wHTML += ('</div>');

            wHTML += ('<div class="col-xs-6">');
            wHTML += ('<div class="box-body bg-white flat border" id="synonyms'+x+'" style="display:none;"  onkeydown="if(event.keyCode == 9 ){ tabEvent(); }" >');
            wHTML += ('<input type="text" class="form-control no-border no-padding" name="synonym-label" id="synonym-label" placeholder="enter synonym" >')
            wHTML += ('</div>');
            wHTML += ('</div>');


            wHTML += ('<div class="col-xs-3" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray" style="padding-top: 5px;">');


            wHTML += ('<a data-toggle="control-sidebar" value="'+x+'" onClick="openSynonyms(this)"><i class="fa fa-object-group" style="padding-right: 5px;" data-toggle="tooltip" title="Define synonyms"></i></a>');
            wHTML += ('<a data-toggle="control-sidebar" onClick="saveEntity ('+x+')"><i class="fa fa-cloud-download" style="padding-right: 5px;" data-toggle="tooltip" title="Save"></i></a>');
            wHTML += ('<a data-toggle="control-sidebar" onClick="deleteEntity ('+x+')"><i class="fa fa-trash-o" data-toggle="tooltip" title="Delete"></i></a>');
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
function saveEntity (elem) {
    //entities[elem];
}
function OnMouseIn (elem) {
    var btn = elem.children[0].children[2];
    btn.style.display = '';
}
function OnMouseOut (elem) {
    var btn = elem.children[0].children[2];
    btn.style.display = 'none';
}
function activeSave(label,value) {
    if ( label.length > 0 )
        saveEntity(value)
}
function addEntity() {
    var row = [];

    var name = "Enter value...";
    row.push(name);

    var description = "description";
    row.push(description);

    entities.push(row);

    showEntities('');
}
function openSynonyms(elem){
    $(elem).toggleClass("text-aqua");
    var x = $(elem).attr('value');
    var node = document.getElementById('synonyms'+x);
    node.style.display = '';
}
function tabEvent(){


}/**
 * Created by Hutoma on 23/08/16.
 */
