var ALERT =
{
    BASIC: { value: 0},
    WARNING: { value: 1},
    DANGER: { value: 2},
    SUCCESS: { value: 3},
    INFO: { value: 4},
    PRIMARY: { value: 5}
};

var API_AI_STATE =
{
    UNDEFINED: { value: 'ai_undefined'},
    QUEUED: { value: 'ai_training_queued'},
    READY_TO_TRAIN: { value: 'ai_ready_to_train'},
    TRAINING: { value: 'ai_training'},
    STOPPED: { value: 'ai_training_stopped'},
    COMPLETED: { value: 'ai_training_complete'},
    ERROR: { value: 'ai_error'}
};

var UI_STATE =
{
    ERROR: {value: -1},
    NOTHING: {value: 0},
    FILE_UPLOADED: {value: 1},
    READY_TO_TRAIN: {value: 2},
    PHASE1_INIT: {value: 3},
    PHASE1_QUEUE: {value: 4},
    PHASE1_RUN: {value: 5},
    PHASE2_INIT: {value: 6},
    PHASE2_RUN: {value: 7},
    STOPPED: {value: 8},
    COMPLETED: {value: 10},
    LISTENING_MODE:{value:999}
};

var UI_TRAINING_STATE =
{
    PHASE1_INIT: {value: 100},
    PHASE1_RUN: {value: 101},
    PHASE1_END: {value: 102},
    PHASE2_INIT: {value: 200},
    PHASE2_RUN: {value: 201}
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