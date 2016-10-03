var msg = new SpeechSynthesisUtterance();
var recognition;
msg.addEventListener('start', function () { });
msg.addEventListener('end', function () { activeSpeechButton(); enableChat();});
msg.lang = 'en-US';
msg.volume = 1;
msg.rate = 1;
msg.pitch = 1;

function setLanguage(lang,voice){
    switch(lang){
        case 'Deutsch':     return 'en-US';
        case 'English':     return 'en-US';
        case 'Français':    return 'en-US';
        case 'Italiano':    return 'it';
        case 'Nederlands':  return 'en-US';
        case 'Português':   return 'en-US';
        default :           return 'en-US';
    }
}

function speak(text) {
    msg.text = text;
    window.speechSynthesis.speak(msg);
}

function loadVoices() {
    msg.voice = speechSynthesis.getVoices().filter(function(voice) { return voice.name == "Google US English"; })[0];
}

function startDictation(human_name, ai_name) {
    blockMultiClick();

    document.getElementById('microphone').setAttribute('class','fa fa-microphone text-light-gray flashing');
    if (window.hasOwnProperty('webkitSpeechRecognition')) {

        recognition = new webkitSpeechRecognition();

        recognition.continuous = false;
        recognition.interimResults = false;

        recognition.lang = "en-US";
        recognition.start();

        recognition.onresult = function(e) {
            document.getElementById('message').value = e.results[0][0].transcript;
            createNodeChat(human_name, ai_name,true);
            recognition.stop();
        };

        recognition.onerror = function(e) {
            recognition.stop();
        }

    }
}

function stopSynthesis(){
    window.speechSynthesis.cancel();
}

function stopDictation(){

}

function blockMultiClick(){
    // block clicking on microphone icon
    document.getElementById('btnSpeech').removeEventListener('click', start);

}


// Chrome loads voices asynchronously.
window.speechSynthesis.onvoiceschanged = function(e) {
    loadVoices();
};
