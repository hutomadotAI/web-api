<?php
require_once "./common/bot.php";

// TODO remove hardcoded part
$bot = new \hutoma\bot();

if ( existsBotInStore($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']))
    $bot = getBot();
else
    $bot = setBasicBotDefaultValues();

$bot->developer = getDeveloper();


function existsBotInStore($aiid){
    $fakelist = array(1 => '6ea04c96-2ec3-4a5a-bd46-81742e38aab0', 2 => 'b9907c0b-3d7f-4919-a635-e964840fb8ab', 3 => '189c8ed5-0fe1-4bc0-ba07-d73bb02aa300',4 => '6214abb0-b9dd-4b5c-8194-2e1df15f89dd',5 => '5e643a75-04da-47e3-b920-546406068808');
    foreach ($fakelist as $singlebot) {
        if ($singlebot == $aiid)
            return true;
    }
    return false;
}

function getBot(){
    // TODO remove this fake hardcoded data
    $infoBot = new \hutoma\bot();
    $infoBot->setName("Fake botName");
    $infoBot->setDescription("Fake shortDescription");
    $infoBot->setLongDescription('A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which I enjoy with my whole heart. I am alone, and feel the charm of existence in this spot, which was created for the bliss of souls like mine. I am so happy, my dear friend, so absorbed in the exquisite sense of mere tranquil existence, that I neglect my talents. I should be incapable of drawing a single stroke at the present moment and yet I feel that I never was a greater artist than now.');
    $infoBot->setUsecase('User: I want to sleep.
Agent: Need a pick-me-up? I can find somewhere nearby to get some coffee.
User: You\'re so sweet.
Agent: I like you too. You\'re a lot of fun to talk to.');
    $infoBot->setAlarmMessage('Questi contenuti non sono disponibili in Italiano. Leggi ulteriori informazioni sulle lingue supportate.');
    $infoBot->setPrivacyLink('https://www.google.it/intl/it/policies/privacy/');
    $infoBot->setUpdate('10 september 2016');  // setted when you send request publish bot
    $infoBot->setLicenceType('Free');
    $infoBot->setLicenceFee('0.0');
    $infoBot->setCategory('Other');
    $infoBot->setClassification('EVERYONE');
    $infoBot->setVersion('1.0.0');
    return $infoBot;
}

function getDeveloper(){
    $infoDeveloper = new \hutoma\developer();
    $infoDeveloper->setName('hu:toma Ltd.');
    $infoDeveloper->setCompany('HUTOMA');
    $infoDeveloper->setEmail('support@hutoma.com');
    $infoDeveloper->setAddress('Carrer del Consell de Cent, 341');
    $infoDeveloper->setPostcode('08007');
    $infoDeveloper->setCity('Barcelona');
    $infoDeveloper->setCountry('Spain');
    $infoDeveloper->setWebsite('http://www.hutoma.com');
    return $infoDeveloper;
}

function setBasicBotDefaultValues(){
    $infoBot = new \hutoma\bot();
    $infoBot->setName($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']);
    $infoBot->setDescription($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description']);
    $infoBot->setLicenceType('Free');
    $infoBot->setClassification('EVERYONE');
    $infoBot->setCategory('No category');
    $infoBot->setLicenceFee('0.0');
    $infoBot->setVersion('1.0.0');
    return $infoBot;
}
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
                        <label for="bot_long_description">Long Description</label>
                        <textarea rows="9" maxlength="5000" class="form-control flat textarea-justify" placeholder="Enter long description..." id="bot_long_description" style="height:204px;"></textarea>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 1 -->
        <br>
        <!-- row 2 -->
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
                <div class="row no-margin">
                    <?php include './dynamic/input.licenceFee.html.php'; ?>
                </div>
                <div class="row no-margin">
                    <?php include './dynamic/input.version.html.php'; ?>
                </div>
            </div>

            <div class="drop-zone-460">
                <div class="row no-margin" style="padding-top:10px;">
                    <div class="form-group no-margin">
                        <label for="bot_usecase">Show Example of Conversation</label>
                        <textarea rows="8" maxlength="2000" class="form-control flat" value="" placeholder="Add sample of conversation..." id="bot_usecase" style="height:182px;"></textarea>
                    </div>
                    <div class="form-group" style="padding-top:15px;">
                        <label for="bot_alert_message">Alert message</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-info-circle"></i>
                            </div>
                            <input type="text" maxlength="150" class="form-control flat no-shadow"  id="bot_alert_message" name="bot_alert_message" placeholder="Enter a message to show..">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="bot_link_privacy">Link Privacy Policy Page</label>
                        <div class="input-group">
                            <div class="input-group-addon">
                                <i class="fa fa-eye"></i>
                            </div>
                            <input type="text" maxlength="1800" class="form-control flat no-shadow"  id="bot_link_privacy" name="bot_link_privacy" placeholder="Enter a link to privacy policy">
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- end row 2 -->
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
                            <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_developer_name" name="bot_developer_name" placeholder="Enter the name of developer...">
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
                            <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_developer_address" name="bot_developer_address" placeholder="Enter the address...">
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
                            <input type="text" maxlength="30" class="form-control flat no-shadow"  id="bot_developer_postcode" name="bot_developer_postcode" placeholder="Enter the postcode...">
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
                            <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_developer_city" name="bot_developer_city" placeholder="Enter the city...">
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
                            <input type="text" maxlength="50" class="form-control flat no-shadow"  id="bot_developer_country" name="bot_developer_country" placeholder="Enter the country...">
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
                            <input type="text" maxlength="100" class="form-control flat no-shadow"  id="bot_developer_email" name="bot_developer_email" placeholder="Enter email...">
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
                            <input type="text" maxlength="1800" class="form-control flat no-shadow"  id="bot_developer_website" name="bot_developer_website" placeholder="Enter the link of website...">
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
                            <input type="text" maxlength="50" class="form-control flat no-shadow" id="bot_developer_company" name="bot_developer_company" placeholder="Enter company...">
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

<div class="modal fade" id="image-modal" role="dialog">
    <div class="modal-dialog flat">
        <!-- Modal content-->
        <div class="modal-content bordered" style="background-color: #202020">
            <div class="modal-body" style="background-color: #535353" >
                <div class="box-body" id="delete-ai-label">
                    <div class="drag-area" id="imagePath">
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <form method="POST" id="deleteForm" action="">
                    <button type="button" class="btn btn-primary flat" id="btnModelCancel" data-dismiss="modal">Cancel
                    </button>
                    <button type="submit" class="btn btn-danger flat" id="btnModalCrop" data-dismiss="modal">Crop
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    var bot = <?php echo $bot->toJSON(); unset($bot);?>;
    var cropper;
</script>

