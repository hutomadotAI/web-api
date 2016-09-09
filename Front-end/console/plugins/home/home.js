document.getElementById("btnCreateAI").addEventListener("click", wizardNext);

function wizardNext() {
    $(this).prop("disabled",true);
    if(document.startForm.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.startForm.submit();
}

function sendAIID(elem){
    var value = elem.value;
    $(elem).prop("disabled",true);
    if(document.viewAllForm.onsubmit)
        return;
    RecursiveUnbind($('#listTable'));
    document.getElementById("ai").value = value;
    document.viewAllForm.submit();
}

function publishAI(elem){
    if ( elem.className == 'btn btn-info flat pull-right'){
        elem.className = 'btn btn-warning flat pull-right';
        elem.innerHTML = '<i class="fa fa-globe"></i> Unpublish AI';
    }
    else{
        elem.className = 'btn btn-info flat pull-right';
        elem.innerHTML = '<i class="fa fa-globe"></i> Publish AI';
    }
}
