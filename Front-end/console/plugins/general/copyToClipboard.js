/**
 * Created by Hutoma on 04/08/16.
 */
function copyToClipboard(elementId) {
    var content = document.getElementById(elementId).getAttribute("placeholder");

    if (content.length != 0) {
        var aux = document.createElement('input');
        aux.value = content;
        document.body.appendChild(aux);
        aux.select();

        var copysuccess;
        try {
            copysuccess = document.execCommand('cut');
        } catch (e) {
            $('#' + elementId + 'tooltip').attr('data-original-title', 'not supported').tooltip('show');
            $('#' + elementId + 'tooltip').attr('data-original-title', 'copy to clipboard');
        }

        if (!copysuccess) {
            $('#' + elementId + 'tooltip').attr('data-original-title', 'not supported').tooltip('show');
            $('#' + elementId + 'tooltip').attr('data-original-title', 'copy to clipboard');
        }
        else {
            $('#' + elementId + 'tooltip').attr('data-original-title', 'Copied!!!').tooltip('show');
            $('#' + elementId + 'tooltip').attr('data-original-title', 'copy to clipboard');
        }
        document.body.removeChild(aux);
    }
}

function RecursiveUnbind($jElement) {
    // remove this element's and all of its children's click events
    $jElement.unbind();
    $jElement.removeAttr('onclick');
    $jElement.removeAttr('href');
    $jElement.children().each(function () {
        RecursiveUnbind($(this));
    });
}