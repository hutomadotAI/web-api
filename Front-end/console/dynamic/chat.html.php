<div class="box box-solid box-clean flat no-shadow direct-chat direct-chat-success">

    <div class="box-header with-border">
        <i class="glyphicon glyphicon-user"></i>
        <h3 class="box-title"><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></h3>

            <div class="pull-right">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="voice options" tabindex="-1" >
                    <i class="fa fa-gears"></i>
                </a>
                <button class="btn btn-box-tool" data-widget="collapse" tabindex="-1" ><i class="fa fa-minus"></i></button>

                <ul class="dropdown-menu flat">
                    <li id="speech-option">
                        <a id="speech-type"><i id="speech-icon" class="fa fa-bullhorn text-light-blue"></i><spam id="speech-text" class="text-light-blue"> Speech Recognition</spam></a>
                    </li>
                    <!--<li class="footer" id="mute-option"><a href="#">  <i class="fa fa-microphone-slash"></i> Mute Microphone</a></li>-->
                    <!--li class="footer" id="color-option"><a href="#">  <i class="fa fa-adjust"></i> Color Voice</a></li>-->
                    <li class="footer" id="json-option" value ="0" onClick="jsonOption(this.value)" onMouseOver="this.style.cursor='pointer'">
                        <a id="json-type"><i id="json-icon" class="fa fa-file-code-o text-light-blue"></i><spam id="json-text" class="text-light-blue"> Show Json Message</spam></a>
                    </li>
                    <li class="divider"></li>
                    <li><a href="#">Confidence</a></li>
                </ul>
            </div>
    </div>
          
    <div class="box-body" id="bodyChat">
        <div class="direct-chat-messages" id="chat">
        </div>
    </div>

    <div class="box-footer" id="chat-footer">
        <div class="input-group">
            <input type="text" id="message" placeholder="Type a message ..." class="form-control" onkeydown="keyboardChat(event)"  tabindex="-1">

            <div class="input-group-addon" id="btnSpeech">
                <i id="microphone" style="font-size: 18px; width:20px;" class="fa fa-microphone text-red"></i>
            </div>
        </div>
    </div>
</div>



  
