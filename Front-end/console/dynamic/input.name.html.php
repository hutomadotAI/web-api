<script type="text/javascript">

    function clearSpaces(input) {
        input.value = input.value.replace(/\s/g, "");
    }

    function blockSpace(event) {
        return event.which != 32;
    }

</script>

<div class="form-group">
    <label>Name</label>
    <div class="input-group">
        <div class="input-group-addon">
            <i class="glyphicon glyphicon-user"></i>
        </div>
        <input type="text" class="form-control" id="ai_name" name="ai_name" placeholder="Enter your AI name"
               onkeydown="return blockSpace(event);" onchange="clearSpaces(this);">
    </div>
</div>

<div class="alert alert-dismissable flat alert-base" id="containerMsgAlertNameAI" style="display:none;">
    <!--<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>-->
    <i class="icon fa fa-check" id="iconAlertNameAI"></i>
    <span id="msgAlertNameAI"></span>
</div>