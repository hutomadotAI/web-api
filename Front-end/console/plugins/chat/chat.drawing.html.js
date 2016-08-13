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


function drawChatFooter(human_name,ai_name) {
    var isChrome = !!window.chrome;
    var wHTML = '';
    var newNode = document.createElement('div');
    newNode.className = 'input-group';
    newNode.id = 'id-input-group';

    wHTML += ('<input type="text" id="message" placeholder="Type Message ..." class="form-control" onkeydown="if(event.keyCode == 13 && this.value ) { createNodeChat(\' '+ human_name +' \', \' '+ ai_name +' \'); }">');

    if (isChrome) {
        wHTML += ('<div class="input-group-addon" id="btnSend"   onClick="startDictation(\' '+ human_name +' \', \' '+ ai_name +' \')" onMouseOver="this.style.cursor=\'pointer\'">');
        wHTML += ('<i id="microphone" style="font-size: 18px;" class="fa fa-microphone text-red "></i>');
        wHTML += ('</div>');
    }
    else {
        wHTML += ('<div class="input-group-addon" id="btnSend" onMouseOver="this.style.cursor=\'pointer\'">');
        wHTML += ('<i id="microphone" style="font-size: 18px;" class="fa fa-microphone-slash text-coral"></i>');
        wHTML += ('</div>');
       /*
        wHTML += ('<input type="text" id="message" placeholder="Type Message ..." class="form-control" onkeydown="if(event.keyCode == 13 && this.value ) { createNodeChat(\' '+ human_name +' \', \' '+ ai_name +' \'); }">');
        wHTML += ('<span class="input-group-btn">');
        wHTML += ('<button type="button" class="btn btn-primary btn-flat" id="btnSend" onClick="createNodeChat(\' '+ human_name +' \', \' '+ ai_name +' \')">Send</button>');
        wHTML += ('</span>');
        */
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
        wHTML += ('<li class="footer"><a href="#">  <i id="voice" class="fa fa-bullhorn" value ="0" onClick="activeVoice(this.value)"></i>Deactive Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-microphone-slash"></i>Mute Microphone</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-adjust"></i>Color Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-retweet"></i>Pepetual Conversation</a></li>');

    }
    else {
        wHTML += ('<li class="footer"><a href="#" disabled="">  <i id="voice" class="fa fa-bullhorn" value ="0" onClick="activeVoice(this.value)"></i>Deactive Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-adjust"></i>Color Voice</a></li>');
        wHTML += ('<li class="footer"><a href="#">  <i class="fa fa-retweet"></i>Pepetual Conversation</a></li>');

    }
    newNode.innerHTML = wHTML;
    document.getElementById('dropdown-chat-options').appendChild(newNode);

}
