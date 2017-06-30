<?php
require "../../pages/config.php";
require_once "../../console/api/apiBase.php";
require_once "../../console/api/integrationApi.php";
require_once "../../console/api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$integrationApi = new \hutoma\api\integrationApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {
    \hutoma\console::redirect('./error.php?err=200');
    exit;
}
$aiid = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'];

if (isset($_GET["action"])) {
    $params = [];
    $params["action"]=$_GET["action"];
    if (isset($_GET["id"])) {
        $params["id"]=$_GET["id"];
    }
    $facebook_action = $integrationApi->facebookAction($aiid, $params);
    if (isset($facebook_action["status"]["info"])) {
        $facebook_msg = $facebook_action["status"]["info"];
    }
}
$facebook_state = $integrationApi->getFacebookConnectState($aiid);
$fb_success = $facebook_state["success"];
$fb_app_id = $facebook_state["facebook_app_id"];
$fb_permissions = $facebook_state["facebook_request_permissions"];
if (!isset($facebook_msg)) {
    $facebook_msg = $facebook_state["integration_status"];
}

$fb_not_connected = true;
$fb_no_page_selected = true;
$fb_empty_pagelist = true;

if (isset($facebook_state)) {
    $fb_not_connected = empty($facebook_state["has_access_token"]);
    $fb_no_page_selected = empty($facebook_state["page_integrated_id"]);
    $fb_empty_pagelist = empty($facebook_state["page_list"]);
}

?>
<script><?php

    // we get the facebook app id from the API call
    // so we can put it here to be picked up by the facebookConnect call
    echo "\nappid = \"$fb_app_id\";";

    // this is the list of permissions that we need to request from
    // facebook when making a connect call
    echo "\npermissions = \"$fb_permissions\";";

?>
</script>

    <?php

    if ($fb_not_connected) {
        ?>
        <div class="box-header with-border">

            <div class="alert alert-base flat no-shadow">
                Integrate this bot with a Facebook Page to allow it to respond on your behalf.
                Users can talk to the bot over Facebook Messenger, both in the app and on the web.
           </div>
            <div style="padding-bottom: 20px">
                <button class="btn btn-primary flat center-block" id="fb-int-connect" style="width: 260px; " alt="disconnect">
                    <i class="fa fa-facebook text-blue"></i>
                    Connect to Facebook
                </button>
            </div>
        </div>
        <?php
    } else {
        $fb_token_expiry = new DateTime($facebook_state["access_token_expiry"]);
    ?>
    <div class="box-header with-border ">
        <div class="alert alert-base flat no-shadow">
            <p>Connected to "<?php echo $facebook_state["facebook_username"];?>" Facebook account</p>
            <p>The access token for this account expires on <?php echo $fb_token_expiry->format("r"); ?></p>
        </div>
        <div style="padding-bottom: 20px">
            <button class="btn btn-primary flat center-block" id="fb-disconnect" style="width: 260px; " alt="disconnect">disconnect
            </button>
        </div>
        <?php
        if ($fb_no_page_selected) {

            if (!$fb_success) {
                ?>
                <div class="alert alert-base flat no-shadow">
                    Could not get a list of pages from Facebook.
                </div>
                <?php
            } elseif ($fb_empty_pagelist) {
                ?>
                <div class="alert alert-base flat no-shadow">
                    This account has no Facebook pages that are suitable for bot integration.
                    Create a new Facebook page or connect to a different Facebook account.
                </div>
                <?php
            } else {
                ?>
                <div class="alert alert-base flat no-shadow">
                    <p>Select a Facebook page to attach this bot to</p>
                    <ul>
                        <?php
                        foreach ($facebook_state["page_list"] as $page_id => $page_name) {
                            echo "<li><a class='fb-page-list' onmouseover=\"this.style.cursor='pointer'\" id=\"_fb_$page_id\">$page_name</a></li>";
                        }
                        ?>
                    </ul>
                </div>
                <?php
            }
        } else {
            ?>

            <div id="containerMsgAlertProgressBar" style="margin-bottom: 0px; padding-right: 0px; display: block;" class="alert alert-dismissable flat alert-base">
                <i id="iconAlertProgressBar" class="icon fa fa-check"></i>
                <span id="msgAlertProgressBar">
                    Integrated with page <a target="_facebook" href="https://www.facebook.com/<?php echo $facebook_state["page_integrated_id"]; ?>">
                    <?php echo $facebook_state["page_integrated_name"]; ?></a>.
                    The bot will respond to all messages sent to that page.
                </span>
            </div>


            <?php
        }
        }
        ?>
    </div>

    <div class="box box-solid box-clean flat no-shadow no-margin" id="newAiintegration" style="padding-bottom: 0px;">
        <div class="box-body" style="padding-bottom: 0px;">
            <div style="padding-bottom: 12px">
                <?php echo $facebook_msg; ?>
            </div>
        </div>
    </div>

