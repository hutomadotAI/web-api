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
    elem.setAttribute('disabled','disabled');

    // color disabled in cascade buttons
    //$('button[name="btnSelectAI"]').css('background', '#afcbe2');

    if(document.viewAllForm.onsubmit)
        return;
    RecursiveUnbind($('#listTable'));
   // deactiveButtons();

    document.getElementById("ai").value = value;
    document.viewAllForm.submit();
}

function recursiveDisable($jElement){
    $jElement.children().each(function () {
        recursiveDisable($(this));
    });
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

function deactiveButtons(){
    document.getElementById('btnCreateAI').setAttribute('disabled','disabled');
}

// VIDEO TUTORIAL
$("#collapseFirstVideoTutorial").on('hidden.bs.collapse', function(){
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' +   '","args":""}', '*');
});