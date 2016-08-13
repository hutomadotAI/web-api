<div class="nav-tabs-custom no-shadow flat">
    <ul class="nav nav-tabs pull-right">
        <li><a href="#trainingweb" data-toggle="tab">from WEB</a></li>
        <li><a href="#trainingbook" data-toggle="tab">from BOOK</a></li>
        <li class="active"><a href="#trainingfile" data-toggle="tab">from FILE</a></li>
        <li class="pull-left text-md"><i class="fa fa-cloud-upload text-info" style="padding-top:10px;padding-left:10px;padding-right:10px;"></i>AI Training</li>
    </ul>


    <div class="tab-content">

        <div class="tab-pane active" id="trainingfile">
            <div class="box-body" id="boxTrainingFile"></div>
        </div>
        
        <div class="tab-pane" id="trainingbook">
            <div class="box-body" id="boxTrainingBook"></div>
        </div>
        
        <div class="tab-pane" id="trainingweb">
            <div class="box-body" id="boxTrainingWeb"></div>
        </div>

    </div>

</div>


<script src="./plugins/training/training.area.js"></script>
<form action="" method="post" enctype="multipart/form-data">
<script type="text/javascript">
    TRAINING.init(["<?php echo $_SESSION['ai_status'] ?>","<?php echo time().$_SERVER['REMOTE_ADDR'].'.txt'?>","<?php echo $_SESSION['current_ai_name']?>"]);
</script>
</form>


