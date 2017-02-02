var msg = new SpeechSynthesisUtterance();
var recognition;
msg.addEventListener('start', function () { });
msg.addEventListener('end', function () { activeSpeechButton(); enableChat();});
msg.volume = 1;
msg.rate = 1;
msg.pitch = 1;

function getLanguageForDictation(lang){
    switch(lang){
        case 'Deutsch':     return 'de-DE';
        case 'English':     return 'en-US';
        case 'Español':     return 'es-ES';
        case 'Français':    return 'fr-FR';
        case 'Italiano':    return 'it-IT';
        case 'Nederlands':  return 'nl-NL';
        default :           return 'en-US';
    }
}

function getVoiceForSpeechSynthesis(lang,gender){
    switch(true){
        case (lang == 'Deutsch' && gender==0): return 'Google Deutsch'; //TODO male not available  - investigate
        case (lang == 'Deutsch' && gender==1): return 'Google Deutsch';
        case (lang == 'English' && gender==0): return 'Daniel';
        case (lang == 'English' && gender==1): return 'Google US English';
        case (lang == 'Español' && gender==0): return 'Diego';
        case (lang == 'Español' && gender==1): return 'Google español';
        case (lang == 'Français' && gender==0): return 'Thomas';
        case (lang == 'Français' && gender==1): return 'Google français';
        case (lang == 'Italiano' && gender==0): return 'Google italiano'; //TODO male not available - investigate
        case (lang == 'Italiano' && gender==1): return 'Google italiano';
        case (lang == 'Nederlands' && gender==0): return 'Xander';
        case (lang == 'Nederlands' && gender==1): return 'Google Nederlands';
        default : return 'Google US English';
    }
}

function speak(text) {
    msg.text = text;
    window.speechSynthesis.speak(msg);
}

function loadVoices(lang,gender) {
    msg.voice = speechSynthesis.getVoices().filter(function(voice) {
        return  voice.name == getVoiceForSpeechSynthesis(lang,gender);
    })[0];
}

function startDictation(human_name, ai_name, language) {
    blockMultiClick();

    document.getElementById('microphone').setAttribute('class','fa fa-microphone text-light-gray flashing');
    if (window.hasOwnProperty('webkitSpeechRecognition')) {

        recognition = new webkitSpeechRecognition();

        recognition.continuous = false;
        recognition.interimResults = false;

        recognition.lang = getLanguageForDictation(language);
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

function blockMultiClick(){
    // block clicking on microphone icon
    document.getElementById('btnSpeech').removeEventListener('click', start);
}

// Chrome loads voices asynchronously.
window.speechSynthesis.onvoiceschanged = function(e) {
    loadVoices(lang,gender);
};
