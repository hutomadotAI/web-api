<div class="box box-solid box-clean flat no-shadow direct-chat direct-chat-success">
    <div class="box-header with-border">
        <i class="fa fa-comment-o"></i>
        <h3 class="box-title"><?php echo $_SESSION['current_ai_name'] ?> - Chat</h3>


        <div class="box-tools pull-right" id="dropdown-chat-options">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" data-toggle="tooltip" title="voice options">
                <i class="fa fa-gears"></i>
            </a>
         
            <button class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-minus"></i></button>
        </div>

    </div>
          
    <div class="box-body">
        <div class="direct-chat-messages" id="chat">
        </div>
    </div>

    <div class="box-footer" id="chat-footer">

    </div>
</div>


<script src="./plugins/chat/chat.drawing.html.js"></script>
<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        drawHTML.init(["<?php echo $_SESSION['user_name'] ?>","<?php echo $_SESSION['current_ai_name'] ?>"]);
    </script>
</form>







  
