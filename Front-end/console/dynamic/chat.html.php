<?php
    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
    }
?>


<div class="box box-solid box-clean flat no-shadow direct-chat direct-chat-success">

    <div class="box-header with-border">
        <i class="fa fa-comments-o text-info"></i>
        <div class="box-title"><b><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></b></div>

            <div class="pull-right">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="voice options" tabindex="-1" >
                    <i class="fa fa-gears text-white"></i>
                </a>

                <ul class="dropdown-menu flat">
                    <li id="speech-option">
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
          
    <div class="box-body" id="bodyChat">
        <input type="hidden" id="chatId" name="chatId" value=""/>
        <div class="direct-chat-messages" id="chat">
        </div>
    </div>

    <div class="box-footer" id="chat-footer">
        <div class="input-group">
            <input type="text" id="message" placeholder="Type a message ..." class="form-control flat no-shadow" onkeydown="keyboardChat(event)"  tabindex="-1"> 

            <div class="input-group-addon" id="btnSpeech">
                <i id="microphone" style="font-size: 18px; width:20px;" class="fa fa-microphone text-red"></i>
            </div>
        </div>
    </div>
</div>




  
