document.getElementById("btnCreateAI").addEventListener("click", wizardCreate);

function wizardCreate() {
    $(this).prop("disabled", true);
    RecursiveUnbind($('#wrapper'));
    window.location.href = './newAI.php';
}

function sendAIID(elem, action) {
    var value = elem.value;
    elem.setAttribute('disabled', 'disabled');
    if (document.viewAllForm.onsubmit)
        return;
    RecursiveUnbind($('#listTable'));
    document.viewAllForm.action = action;
    document.getElementById("ai").value = value;
    document.viewAllForm.submit();
}

function recursiveDisable($jElement) {
    $jElement.children().each(function () {
        recursiveDisable($(this));
    });
}

function drawTableRows() {

    // dependence by aiList from php server
    for (var i = 0, len = aiList.length; i < len; i++) {
        var wHTML = '';
        var newNode = document.createElement('tr');
        wHTML += '<td class="text-gray"style="padding-top: 15px;">' + aiList[i]['name'] + '</td>';
        wHTML += '<td style="padding-top: 15px;">' + aiList[i]['description'] + '</td>';
        wHTML += '<td class="text-center" style="padding-top: 15px;">' + decodeAIState(aiList[i]['ai_status']) + '</td>';

        var publishDisabled = aiList[i]['ai_status'] === 'ai_training_complete' ? 'onClick="sendAIID(this,\'./publishAI.php\')"' : ' data-toggle="tooltip" title="The bot needs to be fully trained before being published" ';
        wHTML += '<td style="padding-top: 8px;padding-right: 0px;">';
        if (aiList[i]['publishing_state'] == "NOT_PUBLISHED") {
            wHTML += '<button type="button" id="btnPublishAI"  value="' + aiList[i]['aiid'] + '"';
            wHTML += publishDisabled + 'class="btn btn-info flat pull-right" style="margin-right: 0px; width: 125px;">' + '<b> <span class="fa fa-globe"></span>';
            wHTML += ' Publish Bot</b></button></td>';
        }
        else if (aiList[i]['publishing_state'] == "SUBMITTED") {
            wHTML += '<button type="button" id="btnPublishAI"  value="' + aiList[i]['aiid'] + '"' + 'onClick="" class="btn btn-warning flat pull-right" style="margin-right: 0px; width: 125px;">' + '<b>';
            wHTML += ' Request Sent</b></button></td>';
        }
        else if (aiList[i]['publishing_state'] == "PUBLISHED") {
            wHTML += '<button type="button" id="btnPublishAI"  value="' + aiList[i]['aiid'] + '"' + 'onClick="" class="btn btn-warning flat pull-right" style="margin-right: 0px; width: 125px;">' + '<b>';
            wHTML += ' Published</b></button></td>';
        }
        else {
            // Don't show any button.
        }

        wHTML += '<td style="padding-top: 8px;padding-right: 0px;">';
        wHTML += '<button type="button" id="btnSelectAI"  value="' + aiList[i]['aiid'] + '"';
        wHTML += 'onClick="sendAIID(this,\'./dynamic/sessionAI.php\')" class="btn btn-primary flat pull-right" style="margin-right: 0px; width: 115px;">';
        wHTML += '<b> <span class="fa fa-search">';
        wHTML += '</span> View Bot </b></button></td>';

        newNode.innerHTML = wHTML;
        document.getElementById('tableAiList').appendChild(newNode);
        var list = document.getElementById('tableAiList');
        list.insertBefore(newNode, list.childNodes[0]);
    }

}

function decodeAIState(state) {
    switch (state) {
        case 'ai_training_stopped' :
            return ('<span class="text-red">Stopped</span>');
            break;
        case API_AI_STATE.READY_TO_TRAIN.value :
        case API_AI_STATE.UNDEFINED.value :
            return ('<span class="text-darkgray">Not Started</span>');
            break;
        case API_AI_STATE.QUEUED.value :
            return ('<span class="text-gray">Queued</span>');
            break;
        case API_AI_STATE.TRAINING.value :
            return ('<span class="text-orange">In Progress</span>');
            break;
        case API_AI_STATE.COMPLETED.value :
            return ('<span class="text-olive">Completed</span>');
            break;
        case API_AI_STATE.ERROR.value :
            return ('<span class="text-red" flat>Error</span>');
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

