var isChrome = !!window.chrome;
var continuousSpeech = '0';
var speechResponse = 1; // voice activated true for default
var jsonResponse   = '1'; // voice activated true for default
var chatSemaphore = 0;


if (isChrome) {
    unlockSpeechOption();
    document.getElementById('btnSpeech').addEventListener('click', start);
    document.getElementById('btnSpeech').style.cursor = 'pointer';
}
else{
    lockSpeechOption();
    document.getElementById('speech-icon').className ='fa fa-bullhorn text-gray';
    document.getElementById('speech-text').className ='text-gray';
    document.getElementById('btnSpeech').setAttribute('title','Available on Chrome');
    document.getElementById('btnSpeech').style.cursor = 'not-allowed';
    document.getElementById('microphone').className ='fa fa-microphone-slash';
    document.getElementById('speech-text').innerHTML = ' Turn On Speech';
    document.getElementById('speech-text').disabled = true;
}

function start(){
    startDictation('human', 'AI');
}

function keyboardChat(e){
    if(e.keyCode == 13 && document.getElementById("message").value )
        createNodeChat('human','AI');
}

function createNodeChat(human_name, ai_name) {
    if ( chatSemaphore == 0) {
        chatSemaphore = (chatSemaphore+1)%(2);
        var msg = $('#message').val();
        var chatId = $('#chatId').val();
        if (msg.length != 0) {
            disableChat();
            deactiveSpeechButton();
            lockSpeechOption();
            createLeftMsg(human_name, msg);
            requestAnswerAI(ai_name, msg, chatId);
        }
    }
}

function createLeftMsg(human_name,msg){
    var height = parseInt( $('#chat').scrollTop());
    var interval = window.setInterval( animate, 10 );

    var newLeftMsg = document.createElement('div');
    newLeftMsg.className = 'direct-chat-msg';
    newLeftMsg.id = 'left_message';
    var date = new Date().toUTCString().split(' ').slice(0, 5).join(' ');
    var wHTML = '';
    wHTML +=('<div class="direct-chat-info clearfix">');
    wHTML +=('<span class="direct-chat-name pull-left" style="color:gray;">');
    wHTML +=(human_name);
    wHTML +=('</span>');
    wHTML +=('<span class="direct-chat-timestamp pull-right">'+date+'</span>');
    wHTML +=('</div>');
    wHTML +=('<img class="direct-chat-img" src="./dist/img/human.jpg" alt="User image">');
    wHTML +=('<div class="direct-chat-text">');
    wHTML += cleanChat(msg);
    wHTML +=('</div>');
    newLeftMsg.innerHTML = wHTML;
    document.getElementById('chat').appendChild(newLeftMsg);

    function animate() {
        if (parseInt( $('#chat')[0].scrollHeight) < parseInt(height) )
            clearInterval( interval );
        height = parseInt(height) + 5;
        $('#chat').scrollTop( height );
    }
}


function createRightMsg(ai_name,msg,chatId,error) {
    // Update chatId if needed
    if ($("#chatId").val() == '') {$("#chatId").val(chatId);}

    var height = parseInt($('#chat').scrollTop());
    var interval = window.setInterval(animate, 10);

    var newLeftMsg = document.createElement('div');
    newLeftMsg.className = 'direct-chat-msg right';
    newLeftMsg.id = 'right_message';

    var date = new Date().toUTCString().split(' ').slice(0, 5).join(' ');
    var wHTML = "";
    wHTML += ('<div class="direct-chat-info clearfix">');
    wHTML += ('<span class="direct-chat-name pull-right" style="color:gray;">');
    wHTML += (ai_name);
    wHTML += ('</span>');
    wHTML += ('<span class="direct-chat-timestamp pull-left">' + date + '</span>');
    wHTML += ('</div>');
    wHTML += ('<img class="direct-chat-img" src="./dist/img/bot.jpg" alt="AI image">');
    if (error)
        wHTML += ('<div class="direct-chat-text bg-warning">');
    else
        wHTML += ('<div class="direct-chat-text bg-primary">');
    wHTML += msg;
    wHTML += ('</div>');
    newLeftMsg.innerHTML = wHTML;
    document.getElementById('chat').appendChild(newLeftMsg);

    function animate() {
        if (parseInt($("#chat")[0].scrollHeight) < parseInt(height))
            clearInterval(interval);
        height = parseInt(height) + 5;
        $('#chat').scrollTop(height);
    }

    if ( speechResponse == 1 && isChrome)
        speak(msg);
    else
        enableChat();

    if (isChrome)
        unlockSpeechOption();
}



function requestAnswerAI(ai_name, question, chatId) {
    if (question == '') {
        return;
    } else {
        jQuery.ajax({
            url: 'chat.php',
            contentType: "application/json; charset=utf-8",
            type: 'GET',
            dataType: 'json',
            data: {chatId: chatId, q: question},
            success: function (response) {
                // Write response in JSON message box
                var JSONnode = document.getElementById('msgJSON');
                JSONnode.innerHTML = JSON.stringify(response, undefined, 2);
                var JSONdata = response;
                if (JSONdata['chatId'] === '') {
                    createRightMsg(ai_name, 'no chat id returned', '', true);
                } else {
                    var chatId = JSONdata['chatId'];
                    if (JSONdata['status']['code'] === 200)
                        createRightMsg(ai_name, JSONdata['result']['answer'], chatId, false);
                    else
                        createRightMsg(ai_name, JSONdata['status']['info'], chatId, true);
                }
            },
            error: function (xhr, ajaxOptions, thrownError) {
                alert(JSON.stringify(xhr.responseText));
                //var JSONdata = JSON.stringify(xhr.responseText);
                //createRightMsg(ai_name, JSONdata['status']['info'], chatId, true);
            }
        });
    }
}

function enableChat(){
    document.getElementById('bodyChat').style.cursor = 'auto';
    document.getElementById('message').disabled = false;
    document.getElementById('message').value = '';
    document.getElementById("message").focus();

    // release block for chatting
    chatSemaphore = (chatSemaphore+1)%(2);
}

function disableChat(){
    document.getElementById('bodyChat').style.cursor = 'progress';
    document.getElementById('message').disabled = true;
    document.getElementById('message').value = '';
}

function activeSpeechButton(){
    if (speechResponse == 1 && isChrome) {
        document.getElementById('btnSpeech').addEventListener('click', start);
        document.getElementById('btnSpeech').style.cursor = 'pointer';
        document.getElementById('microphone').className = ('fa fa-microphone text-red');
        document.getElementById('microphone').disabled = false;
        document.getElementById('speech-text').innerHTML=' Turn Off Speech';

    }
}

function deactiveSpeechButton(){
    if (isChrome) {
        document.getElementById('microphone').disabled = true;
        document.getElementById('btnSpeech').removeEventListener('click', start);
        document.getElementById('btnSpeech').style.cursor = 'not-allowed';
        document.getElementById('microphone').className = 'fa fa-microphone-slash text-coral';
        document.getElementById('speech-text').innerHTML = ' Turn On Speech';
    }
}

function lockSpeechOption(){
    document.getElementById('speech-option').setAttribute('class','disabled');
    document.getElementById('speech-option').setAttribute('onClick','');
}

function unlockSpeechOption(){
    document.getElementById('speech-option').setAttribute('class','');
    document.getElementById('speech-option').setAttribute('onClick','speechOption(this.value)');
}

function speechOption(value){
    if ( speechResponse == 0) {
        speechResponse = (speechResponse+1)%(2);
        activeSpeechButton();
    }
    else {
        // deactive speech buttons
        speechResponse = (speechResponse+1)%2;
        deactiveSpeechButton();
        stopSynthesis();
    }
}

function jsonOption(value){
    jsonResponse = (value+1)%(2);

    // hide json window
    $('#jsonBox').toggle();
}


function copyJsonToClipboard(elementId) {
    var node = document.getElementById('msgJSON');
    var content = (node.innerHTML);

    if ( content.length != 0 && content.trim()){
        var aux = document.createElement('input');

        document.getElementById('message').value = '';
        aux.value = content;

        document.body.appendChild(aux);
        aux.select();

        var copysuccess;
        try{
            copysuccess = document.execCommand('cut');
        }catch(e){
            $('#btnJSON').attr('data-original-title', 'not supported').tooltip('show');
            $('#btnJSON').attr('data-original-title', 'copy to clipboard');
        }
        if(!copysuccess){
            $('#btnJSON').attr('data-original-title', 'not supported').tooltip('show');
            $('#btnJSON').attr('data-original-title', 'copy to clipboard');
        }
        else {
            $('#btnJSON').attr('data-original-title', 'Copied!!!').tooltip('show');
            $('#btnJSON').attr('data-original-title', 'copy to clipboard');
        }
        document.body.removeChild(aux);
    }
    else{
        $('#btnJSON').attr('data-original-title', 'nothing to copy').tooltip('show');
        $('#btnJSON').attr('data-original-title', 'copy to clipboard');
    }
}

function cleanChat(msg){
    msg = msg.replace('\&','&#38');
    msg = msg.replace('\/','&#47');
    return msg.replace('\<','&#60').replace('\>','&#62;');
}

String.prototype.toHtmlEntities = function() {
    return this.replace(/./gm, function(s) {
        return '&#' + s.charCodeAt(0) + ';';
    });
};


