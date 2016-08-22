var drawHTML = drawHTML || (function(){
        var _args = {}; // private
        return {
            init : function(Args) {
                _args = Args;
                drawChatFooter(_args[0],_args[1]);
                drawMenuOptionVoice();
            }
        };
    }());

var continuousSpeech = '0';
var speechResponse = '1'; // voice ativated for default
var colorVoice = '0';
var muteMicrophone = '0';
var chat = 1;  // start enable chatting buttons

function drawChatFooter(human_name,ai_name) {
    var isChrome = !!window.chrome;
    var wHTML = '';
    var newNode = document.createElement('div');
    newNode.className = 'input-group';
    newNode.id = 'id-input-group';

    wHTML += ('<input type="text" id="message" placeholder="Type Message ..." class="form-control" onkeydown="if(event.keyCode == 13 && this.value ) { createNodeChat(\' '+ human_name +' \', \' '+ ai_name +' \'); }">');
    if (isChrome) {
        wHTML += ('<div class="input-group-addon" id="btnSpeech"   onClick="startDictation(\' '+ human_name +' \', \' '+ ai_name +' \')" onMouseOver="this.style.cursor=\'pointer\'">');
        wHTML += ('<i id="microphone" style="font-size: 18px;" class="fa fa-microphone text-red"></i>');
        wHTML += ('</div>');
    }else {
        wHTML += ('<div class="input-group-addon" id="btnSpeech" data-toggle="tooltip" title="Available on Chrome">');
        wHTML += ('<i id="microphone" style="font-size: 18px;" class="fa fa-microphone-slash text-coral"></i>');
        wHTML += ('</div>');
    }
    newNode.innerHTML = wHTML;
    document.getElementById('chat-footer').appendChild(newNode);

}


function drawMenuOptionVoice() {
    var isChrome = !!window.chrome;
    var wHTML = '';
    var newNode = document.createElement('ul');
    newNode.className = 'dropdown-menu flat';
    newNode.id = 'list-options-voice';


    if (isChrome) {
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa fa-bullhorn"></i>Deactive Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-microphone-slash"></i>Mute Microphone</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-adjust"></i>Color Voice</a></li>');
        wHTML += ('<li class="footer" id="conversation-value" value ="0" onClick="activeVoice(this.value)" onMouseOver="this.style.cursor=\'pointer\'"><a id="conversation-type" ><i id="conversation-icon" class="fa fa-retweet"></i><spam id="conversation-task">Continuous Speech</spam></a></li>');
    }
    else {
        wHTML += ('<li class="footer"><a href="#" disabled><i lass="fa fa-bullhorn"></i>Deactive Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-adjust"></i>Color Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-retweet"></i>Pepetual Conversation</a></li>');

    }
    newNode.innerHTML = wHTML;
    document.getElementById('dropdown-chat-options').appendChild(newNode);

}


function activeVoice(value){

    continuousSpeech = (value+1)%(2);
    $('#conversation-value').attr('value', continuousSpeech);
    $('#conversation-icon').toggleClass("text-red");
    $('#conversation-task').toggleClass("text-red");
    if(perpetual == 1) {
        document.getElementById("conversation-task").innerHTML = "One Step Speech";
    }
    else {
        document.getElementById("conversation-task").innerHTML = "Continuous Speech";



        //document.getElementById("SidMenu6").className = "start active";


    }
}