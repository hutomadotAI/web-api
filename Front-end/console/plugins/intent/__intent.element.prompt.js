function showEntries(){

    var wHTML = "";
    var row = [];
    var name = "Enter value...";
    row.push(name);
    var description = "description";
    row.push(description);
    expressionListFromServer.push(row);

    var synonyms = ['ciao','hello','good morning'];
        for (var x in expressionListFromServer) {


            wHTML += ('<div class="col-xs-12">');
            wHTML += ('<div class="box-body bg-white flat" style="border: 1px solid #d2d6de; margin-bottom: 1px;" id="box-entry'+x+'" ondblclick="openSynonyms('+x+')" onmouseover="OnMouseIn (this)" onmouseout="OnMouseOut (this)">');
            wHTML += ('<div class="row">');


            // Entity label
            wHTML += ('<div class="col-xs-3" id="obj-entity">');
            wHTML += ('<input type="text" class="form-control no-border no-padding" tabindex="-1" id="entry-label" placeholder="'+expressionListFromServer[x].name+'"  onkeydown="if(event.keyCode == 13 ){ changeFocus(); }" onkeyup="activeSave(this.value,'+x+')">')
            wHTML += ('</div>');

            //Load previous list ov Synonyms
            wHTML += ('<div class="col-xs-7">');
            wHTML += drawEntriesList(synonyms,x);


            // synonymous input empty
            wHTML += ('<input type="text" class="form-control no-border no-padding" tabindex="0"  placeholder="enter synonym..." onkeydown="checkKeyCode(this,event.keyCode,'+x+')">')
            wHTML += ('</div>');
            wHTML += ('</div>');


            wHTML += ('<div class="col-xs-2" id="btnEnt"  style="display:none;" >');
            wHTML += ('<div class="btn-group pull-right text-gray" style="padding-top: 5px;">');


            wHTML += ('<a data-toggle="control-sidebar" value="'+x+'" onClick="openSynonyms(this)"><i class="fa fa-object-group" style="padding-right: 5px;" data-toggle="tooltip" title="Define synonyms"></i></a>');
            wHTML += ('</div>');
            wHTML += ('</div>');

            wHTML += ('</div>');
            wHTML += ('</div>');
            wHTML += ('</div>');

    }
    var newNode = document.createElement('div');
    newNode.setAttribute('class', 'col-xs-12');
    newNode.setAttribute('style', 'col-xs-12');
    newNode.innerHTML = wHTML;
    document.getElementById('boxAction').appendChild(newNode);
}

function deleteEntity (elem) {
    delete entries[elem];
    showEntities('');
}


function OnMouseIn (elem) {
    var btn = elem.children[0].children[2];
    btn.style.display = '';
}

function OnMouseOut (elem) {
    var btn = elem.children[0].children[2];
    btn.style.display = 'none';
}

function addEntity() {
    var row = [];
    var name = "Enter value...";
    row.push(name);
    var description = "description";
    row.push(description);
    entries.push(row);
    showEntities();
}

function openSynonyms(elem){
    $(elem).toggleClass('text-aqua');
    var x = $(elem).attr('value');
    var node = document.getElementById('list-entry'+x);

    if (node.style.display == 'none') {
        node.style.display = '';
        var parent = document.getElementById('box-entry'+x);
        parent.style.border = '1px solid #00c0ef';

    }
    else {
        if ( node.childElementCount != 1)
            trasformBoxEntriesToText(node);
        node.style.display = 'none';
        var parent = document.getElementById('box-entry'+x);
        parent.style.border = '1px solid #d2d6de';

    }
}

function checkKeyCode(element,key,n_id){
   if(key == 9 || key == 13 || key == 58) {
       if ($(element).val() != "") {
           var synonym = $(element).val();
           var parent = document.getElementById('list-entry'+n_id);
           createNewInputext(element,synonym,parent);
       }
       else {
           var parent =  element.parentNode;
           if ( parent.childElementCount == 1)
               openSynonyms(parent);
       }
   }
}

function createNewInputext(elem,value,parent){
    var wHTML = (value+'<i class="btn fa fa-close" style="margin-top:-5px;" tabindex="1" onClick="removeInput(this)"></i>');
    var newNode = document.createElement('a');
    newNode.setAttribute('class', 'cell-square');
    newNode.setAttribute('style', 'margin-top:5px');
    newNode.innerHTML = wHTML;
    parent.insertBefore(newNode, parent.lastChild);

    elem.value ='';
    elem.setAttribute('placeholder','enter synonym...');
    elem.focus();
}

function removeInput(elem){
    var parent =  elem.parentNode;
    parent.removeChild(elem);
    var parent2 = parent.parentNode;
    parent2.removeChild(parent);
}

function drawEntriesList(synonyms,x) {
    var wHTML ='';
    if (synonyms.length < 0) {
        wHTML += ('<div class="box-body bg-white flat border" id="list-entry' + x + '"  style="display:block;" >');
        for (var y in synonyms) {
            wHTML += ('<a class="cell-square" style="margin-top:5px;">');
            wHTML += (synonyms[y]);
            wHTML += ('<i class="btn fa fa-close" style="margin-top:-5px;" onClick="removeInput(this)"></i>');
            wHTML += ('</a>');
        }
    }else {
        wHTML += ('<div class="box-body bg-white flat border" id="list-entry' + x + '" style="display:none;" >');
    }
    return wHTML;
}

function trasformBoxEntriesToText(parent){
    var simpleText='';
    var children = parent.childNodes;
    alert(children.length);
    for (var i = 0; i < children.length; i++){
        children[i].style.display = 'none';
        if ( (i-1)< children.length )
            simpleText +=  children[i].innerText +' , ';
        else
            simpleText +=  children[i].innerText;
    }
    alert(simpleText);
}


function showBoxEntries(parent){
    var children = parent.childNodes;

    for (var i = 0; i < children.length; i++){
        children[i].style.display = 'block';
        if ( (i-1)< children.length )
            simpleText +=  children[i].innerText +' , ';
        else
            simpleText +=  children[i].innerText;
    }
    alert(simpleText);
}