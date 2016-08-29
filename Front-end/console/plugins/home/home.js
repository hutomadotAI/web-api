document.getElementById("btnCreateAI").addEventListener("click", wizardNext);

function wizardNext() {
    $(this).prop("disabled",true);
    if(document.startForm.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.startForm.submit();
}

function sendAIID(){
    $(this).prop("disabled",true);
    if(document.viewAllForm.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.viewAllForm.submit();
}

function publishAI(){
    if ( this.className == 'btn btn-info flat pull-right'){
        this.className = 'btn btn-warning flat pull-right';
        this.innerHTML = '<i class="fa fa-globe"></i> Unpublish AI';
    }
    else{
        this.className = 'btn btn-info flat pull-right';
        this.innerHTML = '<i class="fa fa-globe"></i> Publish AI';
    }
}
