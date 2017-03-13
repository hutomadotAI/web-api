<?php
    require_once "./common/developer.php";
    require_once "./common/bot.php";
    require_once "./api/botApi.php";

    $botApi = new \hutoma\api\botApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $botDetails = $botApi->getAiBotDetails($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
    unset($botApi);
?>
<div class="row">
    <div class="col-md-12" id="publishInfoBox">
        <div class="alert alert-dismissable flat alert-info unselectable" id="containerMsgAlertPublishInfo" style="padding-bottom: 25px;">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
            <span id="msgAlertPublishInfo" class="text-muted">
                <dt>Information</dt>
                <dl class="dl-horizontal no-margin" style="text-align:justify">
                    This platform is currently under preview. As such the options and features are actively being developed and will likely change. Submitted bots will be reviewed before publishing.
                </dl>
            </span>
        </div>
    </div>
</div>
<div class="box box-solid flat no-shadow drop-zone-580">
    <div class="box-body">
        <!-- row 0 -->
        <div class="row no-margin">
            <div class="col-xs-4 drop-zone">
                <div class="form-group">
                    <label class="unselectable" for="bot_name">Name</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="glyphicon glyphicon-user"></i>
                        </div>
                        <input type="text" maxlength="30" class="form-control flat no-shadow"  id="bot_name" name="bot_name" placeholder="Enter name for publish">
                    </div>
                </div>
            </div>
            <div class="drop-zone1">
                <div class="row no-margin">
                    <div class="form-group">
                        <label class="unselectable" for="bot_description">Short Description</label><a href="./home.php" id="btnHomeBack" class="fa fa-close text-md text-darkgray pull-right"></a>
                        <input type="text" maxlength="50" class="form-control flat no-shadow"  id="bot_description" name="bot_description">
                    </div>
                </div>
            </div>
        </div>

        <!-- row 1 -->
        <div class="row no-margin">
            <div class="col-xs-4 drop-zone">
                <div class="row no-margin">
                    <?php include './dynamic/input.image.html.php'; ?>
                </div>
            </div>

            <div class="drop-zone-460">
                <div class="row no-margin">
                    <div class="form-group">
                        <label class="unselectable" for="bot_longDescription">Long Description</label>
                        <textarea rows="9" maxlength="5000" class="form-control flat textarea-justify" placeholder="Enter long description..." id="bot_longDescription" name="bot_longDescription"style="height:204px;"></textarea>
                    </div>
                </div>
            </div>
        </div>
        <div class="alert alert-dismissable flat alert-danger hidden" id="containerMsgAlertIconUpload" style="margin-top:10px;margin-bottom:0px;">
            <i class="icon fa fa-warning" id="iconAlertIconUpload"></i>
            <span id="msgAlertIconUpload">Uploaded file is not a valid image. Only JPG, PNG files with 205x205 pixel are allowed.</span>
        </div>
        <!-- end row 1 -->
        <p></p>
        <!-- row 2 TOP -->
        <div class="row no-margin"  style="border-top: 1px solid #434343;">
            <div class="col-xs-4 drop-zone2">
                <div class="row no-margin">
                    <?php include './dynamic/input.licenseType.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.category.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.classification.html.php'; ?>
                </div>
            </div>

            <div class="drop-zone-460">
                <div class="row no-margin" style="padding-top:10px;">
                    <div class="form-group no-margin">
                        <label class="unselectable" for="bot_sample">Show Example of Conversation</label>
                        <textarea rows="8" maxlength="2000" class="form-control flat" value="" placeholder="Add sample of conversation..." id="bot_sample" name="bot_sample" style="height:182px;"></textarea>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 2 TOP -->



        <!-- row 2 MIDDLE-->
        <!-- showed only for license subscription  -->
        <div id="collapseLicenseDetailsSubscription" class="panel-collapse collapse">
            <div class="row no-margin" style="border-top: 1px solid #434343; border-bottom: 1px solid #434343;">
                <div class="row no-margin"  style="padding:10px 0px 0px 0px;">
                    <!--
                    <div class="col-xs-4 no-padding">
                        <div class="form-group">
                            <label for="bot_add_field_1">Added field 1</label>
                            <div class="input-group">
                                <div class="input-group-addon">
                                    <i class="fa fa-info"></i>
                                </div>
                                <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_add_field_1" name="bot_add_field_1" placeholder="Enter info...">
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-5">
                        <div class="form-group">
                            <label for="bot_add_field_2">Added field 2</label>
                            <div class="input-group">
                                <div class="input-group-addon">
                                    <i class="fa fa-info"></i>
                                </div>
                                <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_add_field_2" name="bot_add_field_2" placeholder="Enter info...">
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-3 no-padding">
                        <div class="form-group">
                            <label for="bot_add_field_3">Added field 3</label>
                            <div class="input-group">
                                <div class="input-group-addon">
                                    <i class="fa fa-info"></i>
                                </div>
                                <input type="text" maxlength="30" class="form-control flat no-shadow"  id="bot_add_field_3" name="bot_add_field_3" placeholder="Enter info...">
                            </div>
                        </div>
                    </div>
                    -->
                </div>
            </div>
        </div>
        <!-- end row 2 MIDDLE-->

        <!-- row 2 MIDDLE-->
        <!-- showed only for license perpetual  -->
        <div id="collapseLicenseDetailsPerpetual" class="panel-collapse collapse">
            <div class="row no-margin" style="border-top: 1px solid #434343; border-bottom: 1px solid #434343;">
                <div class="row no-margin"  style="padding:10px 0px 0px 0px;">
                   <!--
                    <div class="col-xs-6 no-padding">
                        <div class="form-group">
                            <label for="bot_add_field_1">Added field 1</label>
                            <div class="input-group">
                                <div class="input-group-addon">
                                    <i class="fa fa-info"></i>
                                </div>
                                <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_add_field_1" name="bot_add_field_1" placeholder="Enter info...">
                            </div>
                        </div>
                    </div>
                    <div class="col-xs-6" style="padding-right:0px;">
                        <div class="form-group">
                            <label for="bot_add_field_2">Added field 2</label>
                            <div class="input-group">
                                <div class="input-group-addon">
                                    <i class="fa fa-info"></i>
                                </div>
                                <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_add_field_2" name="bot_add_field_2" placeholder="Enter info...">
                            </div>
                        </div>
                    </div>
                    -->
                </div>
            </div>
        </div>
        <!-- end row 2 MIDDLE-->

        <!-- row 2 BOTTOM-->
        <div class="row no-margin">
            <div class="col-xs-4 drop-zone2">
                <div class="row no-margin">
                    <?php include './dynamic/input.price.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.version.html.php'; ?>
                </div>
            </div>

            <div class="drop-zone-460">
                <div class="row no-margin" style="padding-top:10px;">
                    <div class="form-group">
                        <label class="unselectable" for="bot_alertMessage">Alert message</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-info-circle"></i>
                            </div>
                            <input type="text" maxlength="150" class="form-control flat no-shadow"  id="bot_alertMessage" name="bot_alertMessage" placeholder="Enter a message to show..">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="unselectable" for="bot_privacyPolicy">Link Privacy Policy Page</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-eye"></i>
                            </div>
                            <input type="text" maxlength="1800" class="form-control flat no-shadow"  id="bot_privacyPolicy" name="bot_privacyPolicy" placeholder="Enter a link to privacy policy">
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xs-12 no-padding">
                <div class="form-group">
                    <label class="unselectable" for="bot_videoLink">Link Video Sample</label>
                    <a data-toggle="collapse" href="#collapseVideoLink" tabindex="-1">
                    <div class=" pull-right">more info <i class="fa fa-question-circle text-sm text-yellow"></i></div>
                        </a>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-video-camera"></i>
                        </div>
                        <input type="text" maxlength="1800" class="form-control flat no-shadow"  id="bot_videoLink" name="bot_videoLink" placeholder="Enter a link to a video">
                    </div>
                </div>
            </div>
        </div>
        <div id="collapseVideoLink" class="panel-collapse collapse">
            <div class="box-body no-padding">
                <div class="overlay center-block">
                    <section class="content-info" >
                        <div class="box-body">
                            We support youtube link video.
                        </div>
                    </section>
                </div>
            </div>
        </div>
        <!-- end row 2 BOTTOM-->

        <p></p>
        <!-- row 3 -->
        <div class="row no-margin"  style="border-top: 1px solid #434343;">
            <!-- row 3A -->
            <div class="row no-margin"  style="padding:10px 0px 0px 0px;">
                <div class="col-xs-4 no-padding">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_name">Developer Name</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-university"></i>
                            </div>
                            <input type="text" maxlength="100" class="form-control flat no-shadow unselectable"  id="bot_developer_name" name="bot_developer_name" placeholder="Enter the name of developer..." readonly>
                        </div>
                    </div>
                </div>
                <div class="col-xs-5">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_address">Address</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-building-o"></i>
                            </div>
                            <input type="text" maxlength="100" class="form-control flat no-shadow unselectable"  id="bot_developer_address" name="bot_developer_address" placeholder="Enter the address..." readonly>
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 no-padding">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_postCode">Postcode</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-map-signs"></i>
                            </div>
                            <input type="text" maxlength="30" class="form-control flat no-shadow unselectable"  id="bot_developer_postCode" name="bot_developer_postCode" placeholder="Enter the postcode..." readonly>
                        </div>
                    </div>
                </div>
            </div>

            <!-- row 3B -->
            <div class="row no-margin">
                <div class="col-xs-4 no-padding">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_city">City</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-map-o"></i>
                            </div>
                            <input type="text" maxlength="100" class="form-control flat no-shadow unselectable"  id="bot_developer_city" name="bot_developer_city" placeholder="Enter the city..." readonly>
                        </div>
                    </div>
                </div>
                <div class="col-xs-5">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_country">Country</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-globe"></i>
                            </div>
                            <input type="text" maxlength="50" class="form-control flat no-shadow unselectable"  id="bot_developer_country" name="bot_developer_country" placeholder="Enter the country..." readonly>
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 no-padding">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_email">Email</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-envelope-o"></i>
                            </div>
                            <input type="text" maxlength="100" class="form-control flat no-shadow unselectable"  id="bot_developer_email" name="bot_developer_email" placeholder="Enter email..." readonly>
                        </div>
                    </div>
                </div>
            </div>

            <!-- row 3C -->
            <div class="row no-margin">
                <div class="col-xs-9" style="padding:0px 15px 0px 0px;">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_website">Website</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="glyphicon glyphicon-link"></i>
                            </div>
                            <input type="text" maxlength="1800" class="form-control flat no-shadow unselectable"  id="bot_developer_website" name="bot_developer_website" placeholder="Enter the link of website..." readonly>
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 no-padding">
                    <div class="form-group">
                        <label class="unselectable" for="bot_developer_company">Company</label>
                        <div class="input-group" >
                            <div class="input-group-addon">
                                <i class="fa fa-users"></i>
                            </div>
                            <input type="text" maxlength="50" class="form-control flat no-shadow unselectable" id="bot_developer_company" name="bot_developer_company" placeholder="Enter company..." readonly>
                        </div>
                    </div>
                </div>
            </div>
            <!-- row 3D -->
            <div class="row no-margin">
                <div class="row no-margin" id="alertPublishMessage">
                </div>
            </div>

        </div>
        <!-- end row 3 -->
        <div class="box-footer">
            <a style="width:100px" class="btn btn-primary flat" id="btnBack" onClick="window.location.href='./home.php';"><b>Back</b></a>
            <button class="btn btn-success pull-right flat" id="btnPublishRequest"><b id="btnPublishRequestText">Publish new Bot</b>
                <span class="fa fa-arrow-circle-right" id="iconPublishRequest"></span>
            </button>
        </div>
        <input type="hidden" id="bot_id" name="bot_id" style="display:none;">
        <input type="hidden" id="bot_aiid" name="bot_aiid" style="display:none;">
        <input type="hidden" id="bot_badge" name="bot_badge" style="display:none;">
    </div>
</div>


<script>
    var developer = <?php
        $dev = new \hutoma\developer();
        $dev->setName($developer['info']['name']);
        $dev->setCompany($developer['info']['company']);
        $dev->setEmail($developer['info']['email']);
        $dev->setAddress($developer['info']['address']);
        $dev->setPostcode($developer['info']['postCode']);
        $dev->setCity($developer['info']['city']);
        $dev->setCountry($developer['info']['country']);
        $dev->setWebsite($developer['info']['website']);
        unset($developer);

        $tmp_dev = $dev->toJSON();
        echo json_encode($tmp_dev);
        unset($dev);
        unset($tmp_dev);
        ?>;

    var bot = <?php
        $bot = new \hutoma\bot();
        //TODO need check controll on value returned by API
        /* if (isset($botDetails)!=200) {} */

        if (!empty($botDetails['bot'])){
            $bot->setAiid($botDetails['bot']['aiid']);
            $bot->setAlertMessage($botDetails['bot']['alertMessage']);
            $bot->setBadge($botDetails['bot']['badge']);
            $bot->setBotId($botDetails['bot']['botId']);
            $bot->setCategory($botDetails['bot']['category']);
            $bot->setClassification($botDetails['bot']['classification']);
            $bot->setDescription($botDetails['bot']['description']);
            $bot->setLicenseType($botDetails['bot']['licenseType']);
            $bot->setLongDescription($botDetails['bot']['longDescription']);
            $bot->setName($botDetails['bot']['name']);
            $bot->setPrice($botDetails['bot']['price']);
            $bot->setPrivacyPolicy($botDetails['bot']['privacyPolicy']);
            $bot->setSample($botDetails['bot']['sample']);
            $bot->setVersion($botDetails['bot']['version']);
            $bot->setVideoLink($botDetails['bot']['videoLink']);
        }
        else{
            $bot->setAiid($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
            $bot->setName($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']);
            $bot->setDescription($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description']);
        }

        $tmp_bot = $bot->toJSON();
        unset($bot);

        echo json_encode($tmp_bot);
        unset($tmp_bot);
        ?>;
</script>
<script src="./scripts/drag-and-drop/drag-and-drop.js"></script>
<script src="./scripts/publish/publish.js"></script>