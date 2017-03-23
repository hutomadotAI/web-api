var isChrome = !!window.chrome;
var speechResponse = JSON.parse(speech);
var showJsonWindow = true; // json window showed for default
var chatSemaphore = 0;

if (isChrome) {
    unlockSpeechOption();
    if (!speechResponse) {
        deactiveSpeechButton();
    }
    else
        activeSpeechButton();
}
else {
    lockSpeechOption();
    document.getElementById('speech-icon').className = 'fa fa-microphone text-gray';
    document.getElementById('speech-text').className = 'text-gray';
    document.getElementById('btnSpeech').setAttribute('title', 'Available on Chrome');
    document.getElementById('btnSpeech').style.cursor = 'not-allowed';
    document.getElementById('microphone').className = 'fa fa-microphone-slash';
    document.getElementById('speech-text').innerHTML = ' Turn On Speech';
    document.getElementById('speech-text').disabled = true;
}

function start() {
    startDictation(human, AI);
}

function keyboardChat(e) {
    if (e.keyCode == 13 && document.getElementById("message").value)
        createNodeChat(human, AI);
}

function createNodeChat(human_name, ai_name) {
    if (chatSemaphore == 0) {
        chatSemaphore = (chatSemaphore + 1) % (2);
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

function createLeftMsg(human_name, msg) {
    var height = parseInt($('#chat').scrollTop());
    var interval = window.setInterval(animate, 10);

    var newLeftMsg = document.createElement('div');
    newLeftMsg.className = 'direct-chat-msg';
    newLeftMsg.id = 'left_message';
    var date = new Date().toUTCString().split(' ').slice(0, 5).join(' ');
    var wHTML = '';
    wHTML += ('<div class="direct-chat-info clearfix">');
    wHTML += ('<span class="direct-chat-name pull-left" style="color:gray;">');
    wHTML += (human_name);
    wHTML += ('</span>');
    wHTML += ('<span class="direct-chat-timestamp pull-right">' + date + '</span>');
    wHTML += ('</div>');
    wHTML += ('<img class="direct-chat-img" src="./dist/img/human.jpg" alt="User image">');
    wHTML += ('<div class="direct-chat-text">');
    wHTML += cleanChat(msg);
    wHTML += ('</div>');
    newLeftMsg.innerHTML = wHTML;
    document.getElementById('chat').appendChild(newLeftMsg);

    function animate() {
        if (parseInt($('#chat')[0].scrollHeight) < parseInt(height))
            clearInterval(interval);
        height = parseInt(height) + 5;
        $('#chat').scrollTop(height);
    }
}


function cutText(phrase) {
    const maximumTextLenght = 150; // characters
    if (phrase.length > maximumTextLenght) {
        var chunk = phrase.substr(0, maximumTextLenght);
        return chunk;
    }
    return phrase;
}

function createRightMsg(ai_name, msg, chatId, score, error) {
    // Update chatId if needed
    if ($("#chatId").val() == '') {
        $("#chatId").val(chatId);
    }

    var height = parseInt($('#chat').scrollTop());
    var interval = window.setInterval(animate, 10);

    var newRightMsg = document.createElement('div');
    newRightMsg.className = 'direct-chat-msg right';
    newRightMsg.id = 'right_message';

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
        wHTML += ('<div class="direct-chat-text chat-warning">');
    else
        wHTML += ('<div class="direct-chat-text chat-success">');
    wHTML += msg;
    wHTML += ('</div>');
    if (error == false )
        wHTML += ('<span class="direct-chat-timestamp pull-left text-sm text-white">confidence score: ' + score + '</span>');
    newRightMsg.innerHTML = wHTML;
    document.getElementById('chat').appendChild(newRightMsg);

    function animate() {
        if (parseInt($("#chat")[0].scrollHeight) < parseInt(height))
            clearInterval(interval);
        height = parseInt(height) + 5;
        $('#chat').scrollTop(height);
    }

    if (speechResponse && isChrome)
        speak(cutText(msg));
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
                    createRightMsg(ai_name, 'no chat id returned', '', -1, true);
                } else {
                    var chatId = JSONdata['chatId'];
                    if (JSONdata['status']['code'] === 200)
                        createRightMsg(ai_name, JSONdata['result']['answer'], chatId, JSONdata['result']['score'], false);
                    else
                        createRightMsg(ai_name, JSONdata['status']['info'], chatId, -1, true);
                }
            },
            error: function (xhr, ajaxOptions, thrownError) {
                createRightMsg(ai_name, 'Cannot contact the server.', '', -1, true);
            }
        });
    }
}

function enableChat() {
    document.getElementById('bodyChat').style.cursor = 'auto';
    document.getElementById('message').disabled = false;
    document.getElementById('message').value = '';
    document.getElementById("message").focus();

    // release block for chatting
    chatSemaphore = (chatSemaphore + 1) % (2);
}

function disableChat() {
    document.getElementById('bodyChat').style.cursor = 'progress';
    document.getElementById('message').disabled = true;
    document.getElementById('message').value = '';
}

function activeSpeechButton() {
    if (speechResponse && isChrome) {
        document.getElementById('btnSpeech').addEventListener('click', start);
        document.getElementById('btnSpeech').style.cursor = 'pointer';
        document.getElementById('microphone').className = ('fa fa-microphone text-red');
        document.getElementById('microphone').disabled = false;
        document.getElementById('speech-text').innerHTML = ' Turn Off Speech';

    }
}

function deactiveSpeechButton() {
    if (isChrome) {
        document.getElementById('microphone').disabled = true;
        document.getElementById('btnSpeech').removeEventListener('click', start);
        document.getElementById('btnSpeech').style.cursor = 'not-allowed';
        document.getElementById('microphone').className = 'fa fa-microphone-slash text-coral';
        document.getElementById('speech-text').innerHTML = ' Turn On Speech';
    }
}

function lockSpeechOption() {
    document.getElementById('speech-option').setAttribute('class', 'disabled');
    document.getElementById('speech-option').setAttribute('onClick', '');
}

function unlockSpeechOption() {
    document.getElementById('speech-option').setAttribute('class', '');
    document.getElementById('speech-option').setAttribute('onClick', 'speechOption()');
}

function speechOption() {
    speechResponse = !speechResponse;
    if (speechResponse) {
        activeSpeechButton();
    }
    else {
        // deactive speech buttons
        deactiveSpeechButton();
        stopSynthesis();
    }
    updateVoiceSessionVariable(speechResponse);
}

function updateVoiceSessionVariable(voiceOption){
    jQuery.ajax({
        url: "./dynamic/sessionChat.php",
        type: "POST",
        data: {speech: voiceOption},
        cache: false,
        success: function(response) {
        },
        error: function () {
            console.log('Cannot update speech session variable.');
        }
    });
}

function setOptionJsonWindow() {
    document.getElementById('json-text').innerHTML = ( !showJsonWindow ) ? '  Hide JSON Message' : '  Show JSON Message';
    // toggle json window
    $('#jsonBox').toggle();
    showJsonWindow = !showJsonWindow;
}

function copyJsonToClipboard(elementId) {
    var node = document.getElementById('msgJSON');
    var content = (node.innerHTML);

    if (content.length != 0 && content.trim()) {
        var aux = document.createElement('input');

        document.getElementById('message').value = '';
        aux.value = content;

        document.body.appendChild(aux);
        aux.select();

        var copysuccess;
        try {
            copysuccess = document.execCommand('cut');
        } catch (e) {
            $('#btnJSON').attr('data-original-title', 'not supported').tooltip('show');
            $('#btnJSON').attr('data-original-title', 'copy to clipboard');
        }
        if (!copysuccess) {
            $('#btnJSON').attr('data-original-title', 'not supported').tooltip('show');
            $('#btnJSON').attr('data-original-title', 'copy to clipboard');
        }
        else {
            $('#btnJSON').attr('data-original-title', 'Copied!!!').tooltip('show');
            $('#btnJSON').attr('data-original-title', 'copy to clipboard');
        }
        document.body.removeChild(aux);
    }
    else {
        $('#btnJSON').attr('data-original-title', 'nothing to copy').tooltip('show');
        $('#btnJSON').attr('data-original-title', 'copy to clipboard');
    }
}

function cleanChat(msg) {
    msg = msg.replace('\&', '&#38');
    msg = msg.replace('\/', '&#47');
    return msg.replace('\<', '&#60').replace('\>', '&#62;');
}

String.prototype.toHtmlEntities = function () {
    return this.replace(/./gm, function (s) {
        return '&#' + s.charCodeAt(0) + ';';
    });
};


