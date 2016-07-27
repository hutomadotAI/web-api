<div class="box box-solid box-clean flat no-shadow direct-chat direct-chat-success">
    <div class="box-header with-border">
        <h3 class="box-title"><?php echo $_SESSION['current_ai_name'] ?> - Chat</h3>
        <div class="box-tools pull-right">
              <span data-toggle="tooltip" title="3 New Messages" class="badge bg-light-blue">3</span>
              <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
        </div>
    </div>
          
    <div class="box-body">
        <div class="direct-chat-messages" id="chat">
        </div>
                <!-- Contacts are loaded here -->
            <div class="direct-chat-contacts">
              <ul class="contacts-list">
                <li>
                  <a href="#">
                    <img class="contacts-list-img" src="./dist/img/user1-128x128.jpg">
                    <div class="contacts-list-info">
                      <span class="contacts-list-name">
                        Count Dracula
                          <small class="contacts-list-date pull-right">2/28/2015</small>
                      </span>
                      <span class="contacts-list-msg">
                        How have you been? I was...
                      </span>
                    </div>
                  </a>
                </li>
              </ul>
            </div>
    </div>

    <div class="box-footer">
        <div class="input-group">
          <input type="text" id="message" placeholder="Type Message ..." class="form-control"
                 onkeydown="if(event.keyCode == 13 && this.value ) { createNodeChat('<?php echo $_SESSION['user_name'];?>','<?php echo $_SESSION['current_ai_name'];?>'); }">
          <span class="input-group-btn">
            <button type="button" class="btn btn-primary btn-flat" id="btnSend" onClick="createNodeChat('<?php echo $_SESSION['user_name'];?>','<?php echo $_SESSION['current_ai_name'];?>')">Send</button>
          </span>
        </div>
    </div>
</div>