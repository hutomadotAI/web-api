<?php
    require_once "./common/developer.php";
    require_once "./common/bot.php";
?>

<div class="box box-solid flat no-shadow drop-zone-580">
    <div class="box-body">
        <!-- row 0 -->
        <div class="row no-margin">
            <div class="col-xs-4 drop-zone">
                <div class="form-group">
                    <label for="bot_name">Name</label>
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
                        <label for="bot_description">Short Description</label>
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
                        <label for="bot_longDescription">Long Description</label>
                        <textarea rows="9" maxlength="5000" class="form-control flat textarea-justify" placeholder="Enter long description..." id="bot_longDescription" name="bot_longDescription"style="height:204px;"></textarea>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 1 -->
        <p></p>
        <!-- row 2 TOP -->
        <div class="row no-margin"  style="border-top: 1px solid #434343;">
            <div class="col-xs-4 drop-zone2">
                <div class="row no-margin">
                    <?php include './dynamic/input.licenceType.html.php'; ?>
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
                        <label for="bot_sample">Show Example of Conversation</label>
                        <textarea rows="8" maxlength="2000" class="form-control flat" value="" placeholder="Add sample of conversation..." id="bot_sample" name="bot_sample" style="height:182px;"></textarea>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 2 TOP -->



        <!-- row 2 MIDDLE-->
        <!-- showed only for licence subscription  -->
        <div id="collapseLicenceDetailsSubscription" class="panel-collapse collapse">
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
        <!-- showed only for licence perpetual  -->
        <div id="collapseLicenceDetailsPerpetual" class="panel-collapse collapse">
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
                        <label for="bot_alertMessage">Alert message</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-info-circle"></i>
                            </div>
                            <input type="text" maxlength="150" class="form-control flat no-shadow"  id="bot_alertMessage" name="bot_alertMessage" placeholder="Enter a message to show..">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="bot_privacyPolicy">Link Privacy Policy Page</label>
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
                    <label for="bot_videoLink">Link Video Sample</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="fa fa-video-camera"></i>
                        </div>
                        <input type="text" maxlength="1800" class="form-control flat no-shadow"  id="bot_videoLink" name="bot_videoLink" placeholder="Enter a link to a video">
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 2 BOTTOM-->
        <br>
        
        <!-- row 3 -->
        <div class="row no-margin"  style="border-top: 1px solid #434343;">
            <!-- row 3A -->
            <div class="row no-margin"  style="padding:10px 0px 0px 0px;">
                <div class="col-xs-4 no-padding">
                    <div class="form-group">
                        <label for="bot_developer_name">Developer Name</label>
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
                        <label for="bot_developer_address">Address</label>
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
                        <label for="bot_developer_postcode">Postcode</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-map-signs"></i>
                            </div>
                            <input type="text" maxlength="30" class="form-control flat no-shadow unselectable"  id="bot_developer_postcode" name="bot_developer_postcode" placeholder="Enter the postcode..." readonly>
                        </div>
                    </div>
                </div>
            </div>

            <!-- row 3B -->
            <div class="row no-margin">
                <div class="col-xs-4 no-padding">
                    <div class="form-group">
                        <label for="bot_developer_city">City</label>
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
                        <label for="bot_developer_country">Country</label>
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
                        <label for="bot_developer_email">Email</label>
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
                        <label for="bot_developer_website">Website</label>
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
                        <label for="bot_developer_company">Company</label>
                        <div class="input-group" >
                            <div class="input-group-addon">
                                <i class="fa fa-envelope-o"></i>
                            </div>
                            <input type="text" maxlength="50" class="form-control flat no-shadow unselectable" id="bot_developer_company" name="bot_developer_company" placeholder="Enter company...">
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
            <button class="btn btn-success pull-right flat" id="btnPublishRequest"><b>Publish new Bot</b>
                <span class="fa fa-arrow-circle-right"></span>
            </button>
        </div>
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
        // TODO remove this fake hardcoded data
        $bot = new \hutoma\bot();
        $bot->setName("Fake botName");
        $bot->setDescription("Fake shortDescription");
        $bot->setLongDescription('A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart. I am alone, and feel the charm of existence in this spot, which was created for the bliss of souls like mine. I am so happy, my dear friend, so absorbed in the exquisite sense of mere tranquil existence, that I neglect my talents. I should be incapable of drawing a single stroke at the present moment and yet I feel that I never was a greater artist than now.');
        $bot->setUsecase('User: I want to sleep.
        Agent: Need a pick-me-up? I can find somewhere nearby to get some coffee.
            User: You\'re so sweet.
        Agent: I like you too. You\'re a lot of fun to talk to.');
        $bot->setAlarmMessage('Questi contenuti non sono disponibili in Italiano. Leggi ulteriori informazioni sulle lingue supportate.');
        $bot->setPrivacyLink('https://www.google.it/intl/it/policies/privacy/');
        $bot->setUpdate('10 september 2016');  // setted when you send request publish bot
        $bot->setLicenceType('Free');
        $bot->setLicenceFee('0.0');
        $bot->setCategory('Other');
        $bot->setClassification('EVERYONE');
        $bot->setVersion('1.0.0');
        

        $tmp_bot = $bot->toJSON();
        echo json_encode($tmp_bot);
        unset($bot);
        unset($tmp_bot);
        ?>;
</script>

<script src="./plugins/publish/publish.js"></script>

