document.getElementById("btnMarketplaceSave").addEventListener("click", saveMarketplace);

function saveMarketplace() {

    document.getElementById('btnMarketplaceSave').setAttribute('disabled','disabled');
    msgAlertMarketplace(1,'Saving...');
    $.ajax({
        url : './dynamic/updateAIMesh.php',
        type : 'POST',
        data: { 'AiSkill': userActived },
        //dataType: 'json',
        //processData: false,  // tell jQuery not to process the data
        //contentType: "application/json; charset=utf-8",
        success: function (response) {
            var JSONdata = JSON.parse(response);
            var statusCode = JSONdata['status']['code'];

            if (statusCode === 200) {
                msgAlertMarketplace(4,'Saved!!!');
                //updatePreviousDataLoaded(JSONdata);
                document.getElementById('btnMarketplaceSave').removeAttribute('disabled');
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            msgAlertMarketplace(2,'Something went wrong. Your changes were not saved.');
            document.getElementById('btnMarketplaceSave').removeAttribute('disabled');
        }
    });
}


function showCategory(){
    var myselect = document.getElementById('bot_category');
    var val = parseInt(myselect.options[myselect.selectedIndex].value);

    var str = document.getElementById('search-bot').value;

    // relative values about list of categories
    switch(val){
        case 0:
            showDomains(str, 1,'All');
            break;
        case 1:
            showDomains(str, 1,'Other');
            break;

        //TODO add the list of categories
        default:
            showDomains(str, 1,'');
    }
}

$(function () {
    // disable all possible category in selection before trasfomring in select2
    // it will removed after API passing categories
    var op = document.getElementById('bot_category').getElementsByTagName("option");
    for (var i = 0; i < op.length; i++) {
        op[i].disabled = true;;
    }

    // trasform in select2
    $('.select2').select2();


    // add category 'ALL' for visualize all bots in botstore
    var select = document.getElementById("bot_category");
    var option = document.createElement("option");
    option.text = "All";
    option.value = "0";
    option.selected="selected";
    select.prepend(option);
    document.getElementById('select2-bot_category-container').innerHTML = option.text;

    select.setAttribute('onchange',"showCategory()");
});
