document.getElementById("btnCreateAI").addEventListener("click", wizardNext);

function wizardNext() {
    $(this).prop("disabled",true);
    if(document.startForm.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.startForm.submit();
}

function sendAIID(elem,action){
    var value = elem.value;
    elem.setAttribute('disabled','disabled');

    // color disabled in cascade buttons
    //$('button[name="btnSelectAI"]').css('background', '#afcbe2');

    if(document.viewAllForm.onsubmit)
        return;
    RecursiveUnbind($('#listTable'));
   // deactiveButtons();

    document.viewAllForm.action = action;
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

function drawTableRows() {

    // dependence by aiList from php server
    for (var i = 0, len = aiList.length; i < len; i++) {
        var wHTML = '';
        var newNode = document.createElement('tr');
        wHTML += '<td style="padding-top: 15px;padding-left:0px;">' + aiList[i]['aiid'] + '</td>';
        wHTML += '<td style="padding-top: 15px;">' + aiList[i]['name'] + '</td>';
        wHTML += '<td style="padding-top: 15px;">' + aiList[i]['description'] + '</td>';
        wHTML += '<td class="text-center" style="padding-top: 15px;">' + decodeAIState(aiList[i]['ai_status']) + '</td>';

        wHTML += '<td style="padding-top: 8px;padding-right: 0px;">';
        wHTML += '<button type="button" id="btnPublishAI"  value="' + aiList[i]['aiid'] + '"';
        wHTML += 'onClick="sendAIID(this,\'\')" class="btn btn-info flat pull-right" style="margin-right: 0px; width: 115px;">';
        //wHTML += 'onClick="sendAIID(this,\'./publishAI.php\')" class="btn btn-info flat pull-right" style="margin-right: 0px; width: 115px;">';
        wHTML += '<b> <span class="fa fa-globe">';
        wHTML += '</span> Publish AI </b></button></td>';

        wHTML += '<td style="padding-top: 8px;padding-right: 0px;">';
        wHTML += '<button type="button" id="btnSelectAI"  value="' + aiList[i]['aiid'] + '"';
        wHTML += 'onClick="sendAIID(this,\'./trainingAI.php\')" class="btn btn-primary flat pull-right" style="margin-right: 0px; width: 115px;">';
        wHTML += '<b> <span class="fa fa-search">';
        wHTML += '</span> View AI </b></button></td>';

        newNode.innerHTML = wHTML;
        document.getElementById('tableAiList').appendChild(newNode);
        var list = document.getElementById('tableAiList');
        list.insertBefore(newNode, list.childNodes[0]);
    }

}
function decodeAIState(state) {
    switch (state) {
        case 'STOPPED' :
            return ('<span class="label label-warning">Stopped</span>');
            break;
        case 'NOT_STARTED' :
            return ('<span class="label label-primary">Not Started</span>');
            break;
        case 'QUEUED' :
            return ('<span class="label label-warning">Queued</span>');
            break;
        case 'IN_PROGRESS' :
            return ('<span class="label label-primary">In Progress</span>');
            break;
        case 'STOPPED_MAX_TIME' :
            return ('<span class="label label-danger" >Stopped Max Time</span>');
            break;
        case 'COMPLETED' :
            return ('<span class="label label-success">Completed</span>');
            break;
        case 'ERROR' :
            return ('<span class="label label-danger">Error</span>');
            break;
        case 'MALFORMEDFILE' :
            return ('<span class="label label-danger">Malformed</span>');
            break;
        case 'NOTHING_TO_TRAIN' :
            return ('<span class="label label-danger">Void</span>');
            break;
        default:
            return ('<span class="label label-danger"></span>');
    }
}

// VIDEO TUTORIAL
$("#collapseFirstVideoTutorial").on('hidden.bs.collapse', function(){
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' +   '","args":""}', '*');
});

$(document).ready(function(){
    drawTableRows();

    $(function () {
        $('#tableAi').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": false,
            "ordering": false,
            "info": true,
            "autoWidth": false,
            "pageLength": 5
        });
    });
});
