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
<div class="box box-solid box-clean flat no-shadow unselectable">

    <div class="box-header with-border">
        <div class="box-title"><b>Chat</b></div>
        <a href="#" class="dropdown-toggle pull-right" data-toggle="dropdown" data-toggle="tooltip" title="voice options" tabindex="-1">
            <i class="fa fa-gears text-gray text-sm"></i>
        </a>
        <ul class="dropdown-menu no-border flat pull-right" style="margin-top:2px;margin-right: 10px;">
            <li id="speech-option" onMouseOver="this.style.cursor='pointer'">
                <a id="speech-type"><i id="speech-icon" class="fa fa-microphone text-white"></i><spam id="speech-text" class="text-white"> Turn On Speech</spam></a>
            </li>
            <li class="footer" id="json-option" onClick="setOptionJsonWindow()" onMouseOver="this.style.cursor='pointer'">
                <a id="json-type"><i id="json-icon" class="fa fa-file-code-o text-white"></i><spam id="json-text" class="text-white"> Hide JSON Message</spam></a>
            </li>
        </ul>
    </div>

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

</div>

  
