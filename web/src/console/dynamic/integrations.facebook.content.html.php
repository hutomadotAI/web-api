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
$fb_app_id = $facebook_state["facebook_app_id"];
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
<script> appid = "<?php
    // we get the facebook app id from the API call
    // so we can put it here to be picked up by the facebookConnect call
    echo $fb_app_id;
?>";</script>

    <?php

    if ($fb_not_connected) {
        ?>
        <div class="box-header with-border">

            <div class="alert alert-base flat no-shadow">
                <p>To integrate this bot with Facebook you need allow Hutoma to access your Facebook Page.</p>
            </div>
            <div style="padding-bottom: 20px">
                <button class="btn btn-primary flat" id="fb-int-connect" style="width: 260px; " alt="disconnect">
                    <i class="fa fa-facebook text-blue"></i>
                    Connect to Facebook
                </button>
            </div>
        </div>
        <?php
    } else {
    ?>
    <div class="box-header with-border ">
        <div class="alert alert-base flat no-shadow">
            <p>Connected to account <?php echo $facebook_state["facebook_username"];?></p>
            <p>The access token for this account expires on <?php echo $facebook_state["access_token_expiry"];?></p>
        </div>
        <div style="padding-bottom: 20px">
            <button class="btn btn-primary flat" id="fb-disconnect" style="width: 130px; " alt="disconnect">disconnect
            </button>
        </div>
        <?php
        if ($fb_no_page_selected) {
            if ($fb_empty_pagelist) {
                ?>
                <div class="alert alert-base flat no-shadow">
                    This account has no Facebook pages to integrate with. Create a page or connect to a different Facebook account.
                </div>
                <?php
            } else {
                ?>
                <div class="alert alert-base flat no-shadow">
                    <p>Select a page to integrate this bot with</p>
                    <ul>
                        <?php
                        foreach ($facebook_state["page_list"] as $page_id => $page_name) {
                            echo "<li><a class='fb-page-list' id='_fb_$page_id'>$page_name</a></li>";
                        }
                        ?>
                    </ul>
                </div>
                <?php
            }
        } else {
            ?>
            <div>
                Bot is integrated with page <?php echo $facebook_state["page_integrated_name"]; ?>
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

