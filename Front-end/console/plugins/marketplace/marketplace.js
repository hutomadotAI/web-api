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