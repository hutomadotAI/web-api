var ALERT =
{
    BASIC: {value: 0},
    WARNING: {value: 1},
    DANGER: {value: 2},
    SUCCESS: {value: 3},
    INFO: {value: 4},
    PRIMARY: {value: 5}
};

var API_AI_STATE =
{
    UNDEFINED: {value: 'ai_undefined'},
    QUEUED: {value: 'ai_training_queued'},
    READY_TO_TRAIN: {value: 'ai_ready_to_train'},
    TRAINING: {value: 'ai_training'},
    STOPPED: {value: 'ai_training_stopped'},
    COMPLETED: {value: 'ai_training_complete'},
    ERROR: {value: 'ai_error'}
};

var UI_STATE =
{
    ERROR: {value: -1},
    NOTHING: {value: 0},
    FILE_UPLOADED: {value: 1},
    READY_TO_TRAIN: {value: 2},
    PHASE1_INIT: {value: 3},
    PHASE1_RUN: {value: 4},
    PHASE2_QUEUE: {value: 5},
    PHASE2_INIT: {value: 6},
    PHASE2_RUN: {value: 7},
    STOPPED: {value: 8},
    COMPLETED: {value: 10},
    LISTENING_MODE: {value: 999}
};

var UI_TRAINING_STATE =
{
    PHASE1_INIT: {value: 100},
    PHASE1_RUN: {value: 101},
    PHASE1_END: {value: 102},
    PHASE2_INIT: {value: 200},
    PHASE2_RUN: {value: 201}
};

var INTENT_ACTION =
{
    DELETE_INTENT: {value: false},
    SAVE_INTENT: {value: true}
};

function RecursiveUnbind($jElement) {
    // remove this element's and all of its children's click events
    $jElement.unbind();
    $jElement.removeAttr('onclick');
    $jElement.removeAttr('href');
    $jElement.removeAttr('type');
    $jElement.children().each(function () {
        RecursiveUnbind($(this));
    });
}

function toggleAddBotSkill(node, botId) {
    var MAX_LINKED_BOTS = 5;
    var parent = node.parentNode;

    var listActive = document.getElementById('botsSearch').children[0].getElementsByClassName('borderActive');

    if (!node.classList.contains('switchOn') && listActive.length >= MAX_LINKED_BOTS) {
        alert("You can only combine up to " + MAX_LINKED_BOTS + " bots.");
        return;
    }

    $(node).toggleClass('switchOn');
    if ($(node).attr('data-link') == '0') {
        $(node).attr('data-link', 1);
        parent.setAttribute('data-linked', '1');
        document.getElementById('card' + botId).children[0].children[0].classList.add("borderActive");
    }
    else {
        $(node).attr('data-link', 0);
        parent.setAttribute('data-linked', '0');
        document.getElementById('card' + botId).children[0].children[0].classList.remove("borderActive");
    }
}

$(document).ready(function () {
    var is_chrome = navigator.userAgent.indexOf('Chrome') > -1;
    var is_safari = navigator.userAgent.indexOf("Safari") > -1;
    var is_mac = (navigator.userAgent.indexOf('Mac OS') != -1);
    var is_windows = !is_mac;
    if (is_chrome && is_safari) {
        is_safari = false;
    }
    if (is_safari || is_windows) {
        $('body').css(
            "font-family", "'Century Gothic', CenturyGothic, AppleGothic, 'Helvetica Neue', Helvetica, Arial, sans-serif"
        );
    }
});