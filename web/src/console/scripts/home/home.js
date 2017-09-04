
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

    var view = {
        ais: aiList.map(function(ai, index) {
            ai.canBePublished = ai.publishing_state === "NOT_PUBLISHED" && ai.ai_status === 'ai_training_complete';
            ai.needsTrainingForPublishing = ai.publishing_state === "NOT_PUBLISHED" && ai.ai_status !== 'ai_training_complete';
            ai.submittedForPublishing = ai.publishing_state === "SUBMITTED";
            ai.published = ai.publishing_state === "PUBLISHED";
            ai.trainingStatusString = decodeAIState(ai.ai_status);
            ai.oddEven = index % 2 === 0 ? "odd" : "even";
            return ai;
        }).sort(function(a, b){
            if (a.created_on < b.created_on)
                return 1;
            else if (a.created_on > b.created_on)
                return -1;
            return 0;
        })
    };

    $.get('templates/own_bots_list.mustache', function(template) {
        $('#tableAiList').html(Mustache.render(template, view));
    });
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

