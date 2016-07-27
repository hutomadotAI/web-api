$(document).ready(function() {
    $('#upload').bind("click",function()
    {
        if(!$('#uploadImage').val())
        {
            alert("empty");
            return false;
        }
    });
});