<div class="nav-tabs-custom no-shadow flat unselectable" id="box_upload">
    <ul class="nav nav-tabs pull-right">
        <!-- <li><a href="#trainingweb" data-toggle="tab">Video</a></li> -->
        <li class="text-sm"><a href="#trainingbook" data-toggle="tab">Book Pages</a></li>
        <li class="active text-sm"><a href="#trainingfile" data-toggle="tab">Chat Examples</a></li>
        <li class="pull-left text-md"><i class="fa fa-cloud-upload text-info"
                                         style="padding-top:10px;padding-left:10px;padding-right:10px;"></i><b>AI
                Training</b></li>
    </ul>
    <input type="hidden" id="training-status" name="training-status" value="0"/>
    <input type="hidden" id="training-progress" name="training-error" value="-1"/>
    <input type="hidden" id="training-max-error" name="training-max-error" value="-1"/>
    <div class="tab-content">
        <?php include './dynamic/training.content.upload.file.html.php'; ?>

        <?php include './dynamic/training.content.upload.book.html.php'; ?>

        <?php include './dynamic/training.content.upload.web.html.php'; ?>
    </div>
</div>

