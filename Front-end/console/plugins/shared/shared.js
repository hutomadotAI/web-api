$(window).on('beforeunload', function(){
    $('*').css("cursor", "progress");
});

function RecursiveUnbind($jElement) {
    // remove this element's and all of its children's click events
    $jElement.unbind();
    $jElement.removeAttr('onclick');
    $jElement.removeAttr('href');
    $jElement.children().each(function () {
        RecursiveUnbind($(this));
    });
}