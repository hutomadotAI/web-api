var scale_chart_max_error = 10000;
initializeEventListeners();
initializeConsole(status,training_file,deep_error);

var async_status_UI  = setInterval(function(){ getUIStatusCall()}, 2000);
var async_status_AI  = setInterval(function(){ trainingStatusCall()}, 2000);
var async_error_ping = setInterval(function(){ trainingErrorPing()}, 2000);

function initializeEventListeners(){
    document.getElementById('inputfile').addEventListener('change', enableUploadTextFile);
    document.getElementById('inputstructure').addEventListener('change', enableUploadBookFile);
    document.getElementById('inputurl').addEventListener('keyup', enableUploadUrl);

    document.getElementById('btnUploadFile').addEventListener('click', uploadTextFile);
    document.getElementById('btnUploadStructure').addEventListener('click', uploadBookFile);
    document.getElementById('btnUploadUrl').addEventListener('click', uploadUrl);

    //document.getElementById('zoomIn').addEventListener('click', zoomIn);
}

function initializeConsole(status,file,error){
    setStateResponse(status);
    if (file==1)
        msgAlertUploadFile(4, 'A file is already loaded.');
}

function getUIStatusCall(){
    var state = parseInt(getUICurrentStatus());
    switch (true) {
        case (state == 0):
            break;
        case (state == 1): // File uploaded
            msgAlertProgressBar(1,'Initialising. please wait.');
            phaseOneFlashing(true);
            trainingStartCall();
            break;
        case (state == 2): //  Initialising - Phase One
            //msgAlertProgressBar(1,'Initialising. please wait.');
            break;
        case (state == 3): // Initialising - Phase One - queue
            //msgAlertProgressBar(1,'Initialising. please wait.');
            break;
        case (state == 4): // Execute - Phase One ( simulation ) waiting API
            if (document.getElementById('status-badge-upload').innerHTML == '0%'){
                phaseOneFlashing(false);
                phaseOneReset();
                phaseOneUpdate();
                document.getElementById('show-error').innerText = 'not yet available';
                msgAlertProgressBar(4,'Phase one in progress...');
            }
            break;
        case (state == 5): // Initialising - Phase Two
            phaseOneJump();
            phaseTwoFlashing(true);
            phaseTwoActive();
            msgAlertProgressBar(1,'Initialization may take few minutes. please wait.');
            break;
        case (state == 6): // start phase two
            
            deep_error = getUICurrentError();
            hideChart(false);
            phaseOneJump();
            phaseTwoActive();
            phaseTwoFlashing(false);
            phaseTwoUpdate(deep_error, getUICurrentMaxError());
            document.getElementById('show-error').innerText = deep_error;
            msgAlertProgressBar(4,'Phase two in progress...');
            break;
        case (state == 7):
            msgAlertProgressBar(1,'Training stopped. Please restart training');
            if (!justStopped())
                createMessageWarningInfoAlert();
            break;
        case (state == 10):
            msgAlertProgressBar(3, 'Training completed.');
            hidePreTrainingBar(state);
            break;
        default:
    }
}

function trainingStartCall() {
    jQuery.ajax({
        url: './dynamic/trainingStart.php',
        type: 'GET',
        dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            var JSONdata = response;
            var statusCode = JSONdata['status']['code'];
            //TODO temporary coded - code 400 returned when you upload file on existing file
            if ((statusCode === 200 ) || (statusCode === 400 )) {
                setUICurrentStatus(2);
            } else {
                msgAlertProgressBar(2, 'Training cannot start! code error '+statusCode);
                setUICurrentStatus(-1);
            }
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            setUICurrentStatus(-1);
            msgAlertProgressBar(2, 'Unexpected error occurred during start training');
        }
    });
}

function trainingStatusCall() {
    jQuery.ajax({
        url: './dynamic/trainingStatusAI.php',
        type: 'GET',
        //dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            // TODO JSON validation when API is ready
            setStateResponse(response);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            var JSONdata = JSON.stringify(xhr.responseText);
            setUICurrentStatus(-1);
            msgAlertProgressBar(2, 'Unexpected error occurred during training');
        }
    });
}

function trainingErrorPing() {
    jQuery.ajax({
        url: './dynamic/trainingError.php',
        type: 'GET',
        dataType: 'json',
        processData: false,  // tell jQuery not to process the data
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            // TODO JSON validation when API is ready
            setErrorResponse(response); //  response is deep_learning_error value returned
        },
        error: function (xhr, ajaxOptions, thrownError) {
            setUICurrentStatus(-1);
            msgAlertProgressBar(2, 'Unexpected error occurred during training ping');
        }
    });
}

function setStateResponse(response){
    switch (response) {
        case ('error'):
            setUICurrentStatus(-1); // code -1
            msgAlertProgressBar(2, 'Training was stopped during reading AI status');
            break;
        case 'training_not_started':
            // code 0
            break;
        case 'training_queued' :
            setUICurrentStatus(3);  // code 3
            break;
        case 'training_in_progress':
            if (getUICurrentError() != -1)
                setUICurrentStatus(6); // code 5
            else {
                if (document.getElementById('status-badge-upload').innerHTML == '0%')
                    setUICurrentStatus(4); // code 4
            }
            break;
        case 'training_stopped' :
            setUICurrentStatus(7); // code 6
            break;
        case 'STOPPED_MAX_TIME' :
            break;
        case 'training_completed' :
            setUICurrentStatus(10); // code 10
            break;
        case 'training_nothing_to_train' :
            setUICurrentStatus(-1);
            msgAlertProgressBar(2, 'Nothing to train');
            break;
        case 'internal_error' :
            setUICurrentStatus(-1);
            msgAlertProgressBar(2, 'Internal error');
            break;
        case 'malformed_training_file' :
            setUICurrentStatus(-1);
            msgAlertProgressBar(2, 'Malformed training file');
            break;
        case 'CANCELLED' :
            break;
        default:
            setUICurrentStatus(999);
            break;
    }
}

function setErrorResponse(response){
    switch (response) {
        case ('error'):
            msgAlertProgressBar(2, 'Unexpected error occurred during reading confidence value');
            setUICurrentStatus(-1); 
            break;
        default:
            setUICurrentError(response);
            break;
    }
}

function trainingRestart(){
    disableButtonUploadTextFile(true);
    disableButtonUploadBookFile(true);

    phaseOneReset();
    hideTrainingBar(true);
    hideChart(true);

    disableRestartBoxButton();
    setUICurrentStatus(1);

    disableButtonUploadTextFile(false);
    disableButtonUploadBookFile(false);
}

function getUICurrentStatus(){
    var result = document.getElementById('training-status').value;
    setStateListeningMode();
    return result;
}

function setUICurrentStatus(status){
    document.getElementById('training-status').value = status;
}

function setStateListeningMode(){
    setUICurrentStatus(999);
}

function getUICurrentError(){
    return document.getElementById('training-error').value;
}

function setUICurrentError(error){
    document.getElementById('training-error').value = error.toFixed(7);
}

function getUICurrentMaxError(){
    return document.getElementById('training-max-error').value;
}

function setUICurrentMaxError(error){
    document.getElementById('training-max-error').value = error;
}

function learnRegExp(url){
    return /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/.test(url);
}

function createMessageWarningInfoAlert(){
    var wHTML =''

    wHTML +=('<div class="alert alert-dismissable flat alert-warning" id="containerMsgWarningAlertTrainingInfo">');
    wHTML +=('<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>');
    wHTML +=('<span id="msgAlertWarningTrainingInfo">');
    wHTML +=('<dt>Start info pre-trained neural Manipulation</dt>');
    wHTML +=('<dd>');
    wHTML +=('The goals of the training program should relate directly to the needs determined by the assessment process outlined above.');
    wHTML +=('Course objectives should clearly state what behavior or skill will be changed as a result of the training and should relate');
    wHTML +=('</dd>');
    wHTML +=('<p></p>');
    wHTML +=('<dt class="text-center">');
    wHTML +=('<button class="btn btn-primary btn-md center-block flat" id="restart-button" onclick="trainingRestart();"> <b>Restart Training</b></button>');
    wHTML +=('</dt>');

    wHTML +=('</span>');
    wHTML +=('</div>');

    var parent = document.getElementById('trainingBox');
    parent.innerHTML = wHTML;
}

function getUploadWarnings(info) {
    var warnings = [];
    var maxItemsToShow = 5;
    var itemsToShow = Math.min(info.length, maxItemsToShow);
    for (var i = 0; i < itemsToShow; i++) {
        if (info[i]['key'] === 'UPLOAD_MISSING_RESPONSE') {
            warnings.push("Missing response for '" + info[i]['value'] + "'");
        }
    }
    if (info.length > maxItemsToShow) {
        warnings.push("...");
    }
    return warnings;
}

function haNoContentError(info) {
    for (var i = 0; i < info.length; i++) {
        if (info[i]['key'] === 'UPLOAD_NO_CONTENT') {
            return true;
        }
    }
    return false;
}

function zoomIn(){
    stopChart();
    if ( scale_chart_max_error > 0.0001) {
        scale_chart_max_error = scale_chart_max_error / 5;
        document.getElementById('zoomout').disabled = false;
        document.getElementById('zoomout').className = 'fa fa-minus-circle text-sm text-yellow';
        document.getElementById('zoomout').setAttribute('onClick','zoomOut()');
        startChart();
    }
    else {
        document.getElementById('zoomin').disabled = true;
        document.getElementById('zoomin').className = 'fa fa-plus-circle text-sm text-gray';
        document.getElementById('zoomin').setAttribute('onClick','');
    }
}

function zoomOut(){
    stopChart();
    if ( scale_chart_max_error < 10000) {
        scale_chart_max_error = scale_chart_max_error * 5;
        document.getElementById('zoomin').disabled = false;
        document.getElementById('zoomin').className = 'fa fa-plus-circle text-sm text-yellow';
        document.getElementById('zoomin').setAttribute('onClick','zoomIn()');
        startChart();
    }
    else {
        document.getElementById('zoomout').disabled = true;
        document.getElementById('zoomout').className = 'fa fa-minus-circle text-sm text-gray';
        document.getElementById('zoomout').setAttribute('onClick','');
    }
}



var data = [];
var async_chart_update;

function getData() {
    var totalPoints = 50;

    if (data.length > 0)
        data = data.slice(1);

    while (data.length < totalPoints) {
        if (deep_error != -1)
            data.push(deep_error);
        else
            data.push(scale_chart_max_error);
    }

    var res = [];
    for (var i = 0; i < data.length; ++i) {
        res.push([i, data[i]]);
    }
    return res;
}

function startChart(){
    var interactive_plot = $.plot("#interactive", [getData()], {
        grid: {
            //borderColor: "#f3f3f3",
            //borderWidth: 0,
            //tickColor: "#f3f3f3"
        },
        series: {
            shadowSize: 0, // Drawing is faster without shadows
            color: "#3c8dbc"
        },
        lines: {
            fill: true, //Converts the line chart to area chart
            color: "#3c8dbc"
        },
        yaxis: {
            min: 0,
            max: scale_chart_max_error,
            show: true
        },
        xaxis: {
            show: true
        }
    });

    var updateInterval = 2000; //Fetch data ever x milliseconds
    function update() {
        interactive_plot.setData([getData()]);
        interactive_plot.draw();
        async_chart_update= setTimeout(update, updateInterval);
    }
    update();
}

function stopChart(){
    clearTimeout(async_chart_update);
}
$(function () {


    switch (true) {
        case (deep_error==  -1):
            scale_chart_max_error = 5000; // default scale value
            break;
        case (deep_error<0.00005):
            scale_chart_max_error = 0.00005;
            break;
        case (deep_error<0.0001):
            scale_chart_max_error = 0.0001;
            break;
        case (deep_error<0.001):
            scale_chart_max_error = 0.001;
            break;
        case (deep_error<0.01):
            scale_chart_max_error = 0.01;
            break;
        case (deep_error<0.1):
            scale_chart_max_error = 0.1;
            break;
        case (deep_error<1):
            scale_chart_max_error = 1;
            break;
        case (deep_error<10):
            scale_chart_max_error = 10;
            break;
        case (deep_error<100):
            scale_chart_max_error = 100;
            break;
        case (deep_error<500):
            scale_chart_max_error = 100;
            break;
        case (deep_error<1000):
            scale_chart_max_error = 1000;
            break;
        case (deep_error<5000):
            scale_chart_max_error = 5000;
            break;
        default:
            scale_chart_max_error = 10000;
    }
    startChart();
});

// VIDEO TUTORIAL TRAINING CHAT EXAMPLE
$("#collapseVideoTutorialTraining").on('hidden.bs.collapse', function(){
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' +   '","args":""}', '*');
});

// VIDEO TUTORIAL TRAINING BOOK
$("#collapseVideoTutorialTrainingBook").on('hidden.bs.collapse', function(){
    var iframe = document.getElementsByTagName("iframe")[0].contentWindow;
    iframe.postMessage('{"event":"command","func":"' + 'pauseVideo' +   '","args":""}', '*');
});
