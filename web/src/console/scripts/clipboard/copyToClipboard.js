/**
 * Created by Hutoma on 04/08/16.
 */
function copyToClipboard(elementId) {
    var content = document.getElementById(elementId).getAttribute("value");

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
            
            $('#' + elementId).attr('disabled',false);

            var clipboard = new Clipboard('.input-group-addon');
            clipboard.on('success', function(e) {
                console.log(e);
            });

            clipboard.on('error', function(e) {
                console.log(e);
            });

            $('#' + elementId + 'tooltip').attr('data-original-title', 'Press ⌘-C to copy').tooltip('show');
            $('#' + elementId + 'tooltip').attr('data-original-title', 'copy to clipboard');
        }
        else {
            $('#' + elementId + 'tooltip').attr('data-original-title', 'Copied!!!').tooltip('show');
            $('#' + elementId + 'tooltip').attr('data-original-title', 'copy to clipboard');
        }
        document.body.removeChild(aux);
    }
}
