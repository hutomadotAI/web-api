var msg = new SpeechSynthesisUtterance();
var recognizer;

msg.addEventListener('start', function () {});
msg.addEventListener('end', function () {  enableSpeech(true); enableChat(true);});
msg.lang = 'en-US';
msg.volume = 1;
msg.rate = 1;
msg.pitch = 1


function afterAiSpeech(){

    enableSpeech(true);
    enableChat(true);
    if ( continuousSpeech == 1) {
        // startDictation('andrea','andrea');
    }
    else{
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
    document.getElementById('microphone').setAttribute('class','fa fa-microphone text-light-gray flashing');
    
    if (window.hasOwnProperty('webkitSpeechRecognition')) {

        var recognition = new webkitSpeechRecognition();

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


// Chrome loads voices asynchronously.
window.speechSynthesis.onvoiceschanged = function(e) {
    loadVoices();
};
