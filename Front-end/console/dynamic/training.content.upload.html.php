<script src="./plugins/training/training.area.js"></script>

<div class="box box-solid box-clean flat no-shadow">
    <div class="box-header with-border">
        <h3 class="box-title">Training Area</h3>
        <a data-toggle="collapse"  href="#collapseInfo">
            <div class=" pull-right">more info
            <i class="fa fa-question-circle text-md text-yellow"></i>
            </div>
        </a>
    </div>
    <div class="box-body" id="boxTraining"></div>
    <div class="box-body" id="boxUpload"></div>

</div>


<form action="" method="post" enctype="multipart/form-data">
<script type="text/javascript">
    TRAINING.init(["<?php echo  $_SESSION['ai_status'] ?>","<?php echo time().$_SERVER['REMOTE_ADDR'].'.txt'?>"]);
</script>
</form>


