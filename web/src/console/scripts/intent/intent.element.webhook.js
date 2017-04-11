$(document).ready(function () {
    if (typeof intent['webhook'] == "undefined")
        return;
    $('#webhook').val(intent['webhook']['endpoint']);
});
