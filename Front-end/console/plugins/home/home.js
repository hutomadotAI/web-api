document.getElementById("btnCreateAI").addEventListener("click", wizardNext);

function wizardNext() {
    $(this).prop("disabled", true);
    if (document.startForm.onsubmit)
        return;
    RecursiveUnbind($('#wrapper'));
    document.startForm.submit();
}

function sendAIID(elem, action) {
    var value = elem.value;
    elem.setAttribute('disabled', 'disabled');

    // color disabled in cascade buttons
    //$('button[name="btnSelectAI"]').css('background', '#afcbe2');

    if (document.viewAllForm.onsubmit)
        return;
    RecursiveUnbind($('#listTable'));
    // deactiveButtons();

    document.viewAllForm.action = action;
    document.getElementById("ai").value = value;
    document.viewAllForm.submit();
}

function recursiveDisable($jElement) {
    $jElement.children().each(function () {
        recursiveDisable($(this));
    });
}

function publishAI(elem) {
    if (elem.className == 'btn btn-info flat pull-right') {
        elem.className = 'btn btn-warning flat pull-right';
        elem.innerHTML = '<i class="fa fa-globe"></i> Unpublish AI';
    }
    else {
        elem.className = 'btn btn-info flat pull-right';
        elem.innerHTML = '<i class="fa fa-globe"></i> Publish AI';
    }
}

function deactiveButtons() {
    document.getElementById('btnCreateAI').setAttribute('disabled', 'disabled');
}

function drawTableRows() {

    // dependence by aiList from php server
    for (var i = 0, len = aiList.length; i < len; i++) {
        var wHTML = '';
        var newNode = document.createElement('tr');
        wHTML += '<td style="padding-top: 15px;padding-left:0px;">' + aiList[i]['aiid'] + '</td>';
        wHTML += '<td class="text-gray"style="padding-top: 15px;">' + aiList[i]['name'] + '</td>';
        wHTML += '<td style="padding-top: 15px;">' + aiList[i]['description'] + '</td>';
        wHTML += '<td class="text-center" style="padding-top: 15px;">' + decodeAIState(aiList[i]['ai_status']) + '</td>';

        wHTML += '<td style="padding-top: 8px;padding-right: 0px;">';
        wHTML += '<button type="button" id="btnPublishAI"  value="' + aiList[i]['aiid'] + '"' + 'onClick="sendAIID(this,\'./publishAI.php\')" class="btn btn-info flat pull-right" style="margin-right: 0px; width: 115px;">' + '<b> <span class="fa fa-globe">';
        //TODO for update the state og Publish BUtton
        //if ($.inArray(aiList[i]['aiid'], publishedBots) != -1)
            wHTML += '</span> Publish AI </b></button></td>';
        //else
        //wHTML += '</span> Request Sent</b></button></td>';

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
        case 'ai_training_stopped' :
            return ('<span class="label label-warning">Stopped</span>');
            break;
        case 'ai_ready_to_train' :
            return ('<span class="label label-primary">Not Started</span>');
            break;
        case 'ai_training_queued' :
            return ('<span class="label label-warning">Queued</span>');
            break;
        case 'ai_training' :
            return ('<span class="label label-primary">In Progress</span>');
            break;
        case 'ai_training_complete' :
            return ('<span class="label label-success">Completed</span>');
            break;
        case 'ai_error' :
            return ('<span class="label label-danger">Error</span>');
            break;
        case 'ai_undefined' :
            return ('<span class="label label-primary">None</span>');
            break;
        default:
            return ('<span class="text-red" flat></span>');
    }
}

// VIDEO TUTORIAL
$("#collapseFirstVideoTutorial").on('hidden.bs.collapse', function () {
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' + '","args":""}', '*');
});

$(document).ready(function () {
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
