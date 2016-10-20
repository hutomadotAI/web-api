<div class="nav-tabs-custom no-shadow flat" id="box_upload">

    <ul class="nav nav-tabs pull-right">
        <!-- <li><a href="#trainingweb" data-toggle="tab">Video</a></li> -->
        <li class="text-sm"><a href="#trainingbook" data-toggle="tab">Book Pages</a></li>
        <li class="active text-sm"><a href="#trainingfile" data-toggle="tab">Chat Examples</a></li>
        <li class="pull-left text-md"><i class="fa fa-cloud-upload text-info" style="padding-top:10px;padding-left:10px;padding-right:10px;"></i><b>AI Training</b></li>
    </ul>

    <div class="tab-content">
            <?php include './dynamic/training.content.upload.file.html.php'; ?>

            <?php include './dynamic/training.content.upload.book.html.php'; ?>

            <?php include './dynamic/training.content.upload.web.html.php'; ?>
    </div>

</div>


