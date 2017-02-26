<?php
    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
    }
?>
<script>
    var human =  <?php echo json_encode( $_SESSION[$_SESSION['navigation_id']]['user_details']['first_name'] ); ?>;
    var AI = <?php echo json_encode( $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name'] ); ?>;
    var lang = <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language']); ?>;
    var gender = <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice']); ?>;
</script>

<ul class="sidebar-menu" id="console-menu">
    <div class="box-header no-border text-gray text-center" style="background:#1e282c; color:#8A8A8A; height:35px; padding-top:10px;">

        <div class="input-group">
            <div class="box-title text-gray" style="font-size: 12px; margin-left: 150px;"> CHAT</div>
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="voice options" tabindex="-1" style="margin-left: 125px;">
                <i class="fa fa-gears text-gray text-sm "></i>
            </a>
            <ul class="dropdown-menu no-border flat" style="margin-top:5px;margin-left: 139px;">
                <li id="speech-option" onMouseOver="this.style.cursor='pointer'">
                    <a id="speech-type"><i id="speech-icon" class="fa fa-microphone text-white"></i><spam id="speech-text" class="text-white"> Turn On Speech</spam></a>
                </li>
                <li class="footer" id="json-option" onClick="setOptionJsonWindow()" onMouseOver="this.style.cursor='pointer'">
                    <a id="json-type"><i id="json-icon" class="fa fa-file-code-o text-white"></i><spam id="json-text" class="text-white"> Hide JSON Message</spam></a>
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


  
