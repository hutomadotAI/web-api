var max_error = -1.0;
var block_server_ping = false;

initializedEventListeners();

freezeChat(true);
activeMonitors();

function freezeChat(){
 var text='hey you need to start training before you talk to me!';
}

function initializedEventListeners(){
    document.getElementById('inputfile').addEventListener('change', enableUploadTextFile);
    document.getElementById('inputstructure').addEventListener('change', enableUploadStructure);
    document.getElementById('inputurl').addEventListener('keyup', enableUploadUrl);
    
    document.getElementById('btnUploadFile').addEventListener('click', uploadTextFile);
    document.getElementById('btnUploadStructure').addEventListener('click', uploadStructure);
    document.getElementById('btnUploadUrl').addEventListener('click', uploadUrl);

    //document.getElementById('startstop-button').addEventListener('click', startStop);
}

function activeMonitors(){
    // TODO decide witch is the format returned by API and made response result control

    if ( training_file == 1 ) {
        jumpPhaseOne();

        switch (status) {
            case 'training_not_started' :  // 'NOT_STARTED' :
                trainingStartCall();
                break;
            case 'training_stopped' : // 'STOPPED' :
                createMessageWarningInfoAlert();
                msgAlertProgressBar(1,'Training stopped. Please restart training');
                break;
            case 'training_queued' :  // 'QUEUED' :
                msgAlertProgressBar(1,'Request training in queue');
                break;
            case 'training_in_progress':  // 'IN_PROGRESS' :
                activeTrainingTextFilePhaseTwo();
                pingTrainingError();
                break;
            case 'STOPPED_MAX_TIME' :
                break;
            case 'training_completed' :  //'COMPLETED' :
                jumpPhaseTwo();
                break;
            case 'DELETED' :
                break;
            case 'ERROR' :
                break;
            case 'MALFORMEDFILE' :
                break;
            case 'CANCELLED' :
                break;
            default :
                break;
        }
    }
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


function notifyError(){
    $('#progress-upload-file-action').removeClass('active');
    $('#progress-upload-file-action').removeClass('progress-striped');
    msgAlertProgressBar(2,'Internal error occured');
}

function pingError(){
    var time_ping = 7000; // milliseconds
    var value = document.getElementById("progress-training-file").getAttribute('value');
    var precision_limit = 0.009;

    if( (parseInt(value) < 100) && !block_server_ping ) {
        var error = trainingErrorCall();

        if ( error > precision_limit ) {
            if (parseFloat(error) > parseFloat(max_error))
                max_error = error;
            updateTrainingBar(error, max_error);
            setTimeout(pingError, time_ping);
        }else {
            jumpPhaseTwo();
        }

    }else {
        jumpPhaseTwo();
    }
}

function pingTrainingError(){
    var pingErrorValue = trainingErrorCall();

    if(  pingErrorValue == 'error' )
        return 'error';

    if(  pingErrorValue == -1 )
        setTimeout(pingTrainingError, 6000);
    else {
        initialisingStatusTrainingBar(false);
        pingError();
    }
}

function trainingErrorCall(){
    var xmlhttp;
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');
    xmlhttp.open('GET','./dynamic/trainingError.php',false); // blocking response
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            // TODO json validation when API is ready
            var JSONresponse = xmlhttp.responseText;
            return JSONresponse;
        }
        else
            return 'error';
    };
    xmlhttp.send();
    return xmlhttp.onreadystatechange();
}

function getAiStatusCall(){
    var xmlhttp;
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');
    xmlhttp.open('GET','./dynamic/trainingStatusAI.php',false); // wait response
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            // TODO json validation when API is ready
            var JSONresponse = xmlhttp.responseText;
            return JSONresponse;
        }
        else
            return 'error';
    };
    xmlhttp.send();
    return xmlhttp.onreadystatechange();
}

function existsAiTrainingFileCall(){
    var xmlhttp;
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');
    xmlhttp.open('GET','./dynamic/trainingFile.php',false); // wait response
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var JSONresponse = xmlhttp.responseText;
            try {
                var JSONdata = JSON.parse(JSONresponse);
                // TODO json validation when API is ready
                return JSONresponse
            } catch (e) {
                return 'error';
            }
        }
        else
            return 'error';
    };
    xmlhttp.send();
    return xmlhttp.onreadystatechange();
}

function trainingRestart(){
    console.log('call restart training');

    resetTrainingTextFilePhaseTwoComponents();
    disableButtonUploadTextFile(true);

    resetTrainingTextFilePhaseOneComponents();
    updateTrainingTextFilePhaseOneComponents();
    // if exits box-warning re-start - it needs disabled
    hideMsgWarningAlertTrainingInfo();
    trainingStartCall();

    disableButtonUploadTextFile(false);


}

function trainingStartCall(){
    var xmlhttp;
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');
    xmlhttp.open('GET','./dynamic/trainingStart.php',false); // wait response
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var JSONresponse = xmlhttp.responseText;
            try {
                var JSONdata = JSON.parse(JSONresponse);
                var statusCode = JSONdata['status']['code'];
                if ( (statusCode === 200 ) || (statusCode === 400 ) ) {
                    msgAlertProgressBar(0,'Phase 2 training in progress... ');
                    activeTrainingTextFilePhaseTwo();
                    pingTrainingError();
                } else {
                    msgAlertProgressBar(2,'Training not started');
                }
            } catch (e) {
                msgAlertProgressBar(2,'Training fatal error');
            }
        }else {

        }
    };
    xmlhttp.send();
}


function trainingStopCall(){
    var xmlhttp;
    if (window.XMLHttpRequest)
        xmlhttp = new XMLHttpRequest();
    else
        xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');
    xmlhttp.open('GET','./dynamic/trainingStop.php',false); // wait response
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var JSONresponse = xmlhttp.responseText;
            try {
                var JSONdata = JSON.parse(JSONresponse);
                var statusCode = JSONdata['status']['code'];
                if (statusCode === 200) {
                } else {
                }
            } catch (e) {
                alert('error stop training');
            }
        }else {

        }
    };
    xmlhttp.send();
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
    wHTML +=('<button class="btn btn-primary btn-md center-block flat" id="restart-button" onclick="trainingRestart();"> <b>Restart Trainig</b></button>');
    wHTML +=('</dt>');

    wHTML +=('</span>');
    wHTML +=('</div>');

    var parent = document.getElementById('trainingBox');
    parent.innerHTML = wHTML;
}

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