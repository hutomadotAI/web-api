<?php
    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
    }
?>
<ul class="sidebar-menu" id="console-menu">
    <li class="header" style="text-align: center;color:#8A8A8A;">CHAT</li>
</ul>

<div class="box-header no-border text-gray" style="background: #2e3032">
    <i class="fa fa-user text-olive"></i>
    <div class="box-title text-center" ><b><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></b></div>

    <div class="pull-right">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="voice options" tabindex="-1" >
            <i class="fa fa-gears text-gray"></i>
        </a>

        <ul class="dropdown-menu no-border flat">
            <li id="speech-option" onMouseOver="this.style.cursor='pointer'">
                <a id="speech-type"><i id="speech-icon" class="fa fa-microphone text-white"></i><spam id="speech-text" class="text-white"> Turn Off Speech</spam></a>
            </li>
            <!--<li class="footer" id="mute-option"><a href="#">  <i class="fa fa-microphone-slash"></i> Mute Microphone</a></li>-->
            <!--li class="footer" id="color-option"><a href="#">  <i class="fa fa-adjust"></i> Color Voice</a></li>-->

            <li class="footer" id="json-option" value ="0" onClick="jsonOption(this.value)" onMouseOver="this.style.cursor='pointer'">
                <a id="json-type"><i id="json-icon" class="fa fa-file-code-o text-white"></i><spam id="json-text" class="text-white"> Show Json Message</spam></a>
            </li>
            <!--
            <li class="divider"></li>
            <li><a href="#">Confidence</a></li>
            -->
        </ul>
    </div>
</div>
<div class="box-footer flat" id="chat-footer">
    <div class="input-group">
        <input type="text" id="message" placeholder="Type a message ..." class="form-control flat no-shadow" onkeydown="keyboardChat(event)"  tabindex="-1">
        <div class="input-group-addon" id="btnSpeech">
            <i id="microphone" style="font-size: 18px; width:20px;" class="fa fa-microphone text-red"></i>
        </div>
    </div>
</div>
<div class="box box-solid flat no-shadow direct-chat-success no-margin"  style="background: #2e3032;">
    <div class="box-body flat" id="bodyChat"  style="margin-top: 1px">
        <input type="hidden" id="chatId" name="chatId" value=""/>
        <div class="direct-chat-messages" id="chat">
        </div>
    </div>
</div>




  
