var isChrome = !!window.chrome;
var continuousSpeech = '0';
var speechResponse = '1'; // voice activated true for default
var jsonResponse   = '1'; // voice activated true for default
var colorVoice = '0';
var muteMicrophone = '0';
var chat = 1;  // start enable chatting buttons


if (isChrome)
    document.getElementById('btnSpeech').addEventListener('click', start);
else{


    document.getElementById('deactive-icon').setAttribute('class','fa fa-bullhorn text-gray');
    document.getElementById('deactive-text').setAttribute('class','text-gray');
    document.getElementById("btnSpeech").setAttribute('title','Available on Chrome');
    document.getElementById("btnSpeech").setAttribute('title','Available on Chrome');
    document.getElementById("microphone").className ='fa fa-microphone-slash text-coral';

}

function start(){
    startDictation('human', 'AI');
}

function keyboardChat(e){
    if(e.keyCode == 13 && document.getElementById("message").value )
     createNodeChat('human','AI');
}

function createNodeChat(human_name, ai_name) {
   if ( chat == 1) {
       chat = (chat+1)%(2);
       var msg = $('#message').val();
       if (msg.length != 0) {
           createLeftMsg(human_name, msg);
           requestAnswerAI(ai_name, msg);
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
    wHTML +=('<span class="direct-chat-name pull-left">');
    wHTML +=(human_name);
    wHTML +=('</span>');
    wHTML +=('<span class="direct-chat-timestamp pull-right">'+date+'</span>');
    wHTML +=('</div><!-- /.direct-chat-info -->');
    wHTML +=('<img class="direct-chat-img" src="./dist/img/user1-128x128.jpg" alt="User image">');
    wHTML +=('<div class="direct-chat-text bg-white">');
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


function createRightMsg(ai_name,msg,error) {
    var height = parseInt($('#chat').scrollTop());
    var interval = window.setInterval(animate, 10);

    var newLeftMsg = document.createElement('div');
    newLeftMsg.className = 'direct-chat-msg right';
    newLeftMsg.id = 'right_message';

    var date = new Date().toUTCString().split(' ').slice(0, 5).join(' ');
    var wHTML = "";
    wHTML += ('<div class="direct-chat-info clearfix">');
    wHTML += ('<span class="direct-chat-name pull-right">');
    wHTML += (ai_name);
    wHTML += ('</span>');
    wHTML += ('<span class="direct-chat-timestamp pull-left">' + date + '</span>');
    wHTML += ('</div>');
    wHTML += ('<img class="direct-chat-img" src="./dist/img/user3-128x128.jpg" alt="AI image">');
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
    if ( speechResponse == 1)
        speak(msg);
    else
        enableChat(true);
}


function requestAnswerAI(ai_name, question) {
    var xmlhttp;
    if (question == '') {
        return;
    } else {
        if (window.XMLHttpRequest)
            xmlhttp = new XMLHttpRequest();
        else
            xmlhttp = new ActiveXObject('Microsoft.XMLHTTP');

        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                var JSONnode = document.getElementById('msgJSON');
                var JSONresponse = xmlhttp.responseText;
                var JSONdata = JSON.parse(JSONresponse);
                JSONnode.innerHTML = JSONresponse;
                if(JSONdata['status']['code'] === 200)
                    createRightMsg(ai_name, JSONdata['result']['answer'],false);
                else
                    createRightMsg(ai_name, JSONdata['status']['info'],true);
            }
        };
        xmlhttp.open('GET','chat.php?q='+question,true);
        xmlhttp.send();
    }
}

function enableChat(flag){
    if(flag) {
        document.getElementById('message').disabled = false;
        document.getElementById('message').value = '';
        document.getElementById("message").focus();
        chat = (chat+1)%(2);
    }
    else{
        document.getElementById('message').disabled = true;
        document.getElementById('message').value = '';
    }
}


function enableSpeech(flag){
    if(flag) {

        // document.getElementById('microphone').onclick = startDictation(+ human_name +' \', \' '+ ai_name +' \'";
        document.getElementById('microphone').disabled = false;
        document.getElementById('microphone').setAttribute('class', 'fa fa-microphone text-red');
    }
    else{
        document.getElementById('microphone').disabled = true;
        document.getElementById('microphone').setAttribute('class', 'fa fa-microphone text-coral');
    }
}


function continuousOption(value){
    continuousSpeech = (value+1)%(2);
    $('#continuous-option').attr('value', continuousSpeech);
    $('#continuous-icon').toggleClass("text-red");
    $('#continuous-text').toggleClass("text-red");
}

function jsonOption(value){
    jsonResponse = (value+1)%(2);
    $('#json-icon').toggleClass("text-red");
    $('#json-text').toggleClass("text-red");
    $('#jsonBox').toggle();
}


function copyToClipboard(elementId) {
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


