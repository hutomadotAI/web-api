<?php
namespace hutoma;

$currentAi = sessionObject::getCurrentAI();
$aiName = json_encode($currentAi['name']);
$language = json_encode($currentAi['language']);
$voice = json_encode($currentAi['voice']);

$userInfo = sessionObject::getCurrentUserInfoDetailsMap();
$humanName = json_encode($userInfo['name']);
$speech = isset($userInfo['speech']) ? json_encode($userInfo['speech']) : 'false';
?>

<script>
    var human =  <?php echo $humanName ?>;
    var AI = <?php echo $aiName ?>;
    var lang = <?php echo $language ?>;
    var gender = <?php echo $voice ?>;
    var speech = <?php echo $speech ?>;
</script>

<ul class="sidebar-menu" id="console-menu">
    <div class="box-header no-border text-gray text-center" style="background:#1e282c; color:#8A8A8A; height:35px; padding-top:10px;">

        <div class="input-group">
            <div class="box-title text-gray" style="font-size: 12px; margin-left: 150px;"> CHAT</div>
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" title="voice options" tabindex="-1" style="margin-left: 125px;">
                <i class="fa fa-gears text-gray text-sm "></i>
            </a>
            <ul class="dropdown-menu no-border flat" style="margin-top:5px;margin-left: 139px;">
                <li id="speech-option" onMouseOver="this.style.cursor='pointer'">
                    <a id="speech-type"><i id="speech-icon" class="fa fa-microphone text-white"></i><span id="speech-text" class="text-white"> Turn On Speech</span></a>
                </li>
                <li class="footer" id="json-option" onClick="setOptionJsonWindow()" onMouseOver="this.style.cursor='pointer'">
                    <a id="json-type"><i id="json-icon" class="fa fa-file-code-o text-white"></i><span id="json-text" class="text-white"> Hide JSON Message</span></a>
                </li>
            </ul>
        </div>
        
    </div>
</ul>

<div class="box box-solid flat no-shadow no-margin"  style="background: #212121;">
    <div class="box-body flat" id="bodyChat"  style="margin-top: 1px">
        <input type="hidden" id="chatId" name="chatId" value=""/>
               <div class="direct-chat-messages" id="chat" style="background: #434343;">
               </div>
    </div>
</div>

<div class="box-footer flat" id="chat-footer">
    <div class="input-group">
        <input type="text" id="message" placeholder="Type a message ..." class="form-control flat no-shadow" onkeydown="keyboardChat(event)"  tabindex="-1">
        <div class="input-group-addon" id="btnSpeech">
            <i id="microphone" style="font-size: 18px; width:20px;"></i>
        </div>
    </div>
</div>


  
