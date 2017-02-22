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

function getPercentualValue(error){
    error = 100 - error;
    error = error.toFixed(2);
    if ( error % 1 === 0 )
        error = Math.round(error);
    return error
}

$(document).ready(function(){
    var is_chrome = navigator.userAgent.indexOf('Chrome') > -1;
    var is_safari = navigator.userAgent.indexOf("Safari") > -1;
    var is_mac = (navigator.userAgent.indexOf('Mac OS') != -1);
    var is_windows = !is_mac;
    if (is_chrome && is_safari){
        is_safari=false;
    }
    if (is_safari || is_windows){
        $('body').css(
            "font-family", "'Century Gothic', CenturyGothic, AppleGothic, 'Helvetica Neue', Helvetica, Arial, sans-serif"
        );
    }
});