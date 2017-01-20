var ALERT =
{
    BASIC: { value: 0},
    WARNING: { value: 1},
    DANGER: { value: 2},
    SUCCESS: { value: 3},
    INFO: { value: 4},
    PRIMARY: { value: 5},
};


$(window).on('beforeunload', function(){
    $('*').css("cursor", "progress");
});

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