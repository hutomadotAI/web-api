<?php

namespace hutoma;

use DateTime;

require_once __DIR__ . "/../common/errorRedirect.php";
require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/integrationApi.php";
require_once __DIR__ . "/../api/botstoreApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

$integrationApi = new api\integrationApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());

if (!isset(sessionObject::getCurrentAI()['aiid'])) {
    errorRedirect::defaultErrorRedirect();
    exit;
}

// get the aiid from the session
$aiid = sessionObject::getCurrentAI()['aiid'];

// if this happens right after a connect ...
if (isset($_SESSION[$_SESSION['navigation_id']]['fb_connect_result'])) {
    $connect_result = $_SESSION[$_SESSION['navigation_id']]['fb_connect_result'];
    // load the result of that connect
    unset($_SESSION[$_SESSION['navigation_id']]['fb_connect_result']);
    // if there was a connect warning message then display that
    if (($connect_result["status"]["code"] === 409) && isset($connect_result["status"]["info"])) {
        $facebook_msg = $connect_result["status"]["info"];
    }
}

if (isset($_GET["action"])) {
    $params = [];
    $params["action"] = $_GET["action"];
    if (isset($_GET["id"])) {
        $params["id"] = $_GET["id"];
    }
    $facebook_action = $integrationApi->facebookAction($aiid, $params);
    if (!isset($facebook_msg) && (isset($facebook_action["status"]["info"]))) {
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
    window.fbAsyncInit = function () {
        FB.init({
            appId: appid,
            xfbml: true,
            version: "v2.6"
        });

    };

    (function (d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) {
            return;
        }
        js = d.createElement(s);
        js.id = id;
        js.src = "//connect.facebook.net/en_US/sdk.js";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));

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
            <button class="btn btn-primary flat center-block" id="fb-int-connect" style="width: 260px; "
                    alt="disconnect">
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
        <p>Connected to "<?php echo $facebook_state["facebook_username"]; ?>" Facebook account</p>
        <p>The access token for this account expires on <?php echo $fb_token_expiry->format("r"); ?></p>
    </div>
    <div style="padding-bottom: 20px">
        <button class="btn btn-primary flat center-block" id="fb-disconnect" style="width: 260px; " alt="disconnect">
            disconnect
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

        <div id="containerMsgAlertProgressBar" style="margin-bottom: 32px; padding-right: 0px; display: block;"
             class="alert alert-dismissable flat alert-base">
            <i id="iconAlertProgressBar" class="icon fa fa-check"></i>
            <span id="msgAlertProgressBar">
                    Integrated with page <a target="_facebook"
                                            href="https://www.facebook.com/<?php echo $facebook_state["page_integrated_id"]; ?>">
                    <?php echo $facebook_state["page_integrated_name"]; ?></a>.
                    The bot will respond to all messages sent to that page.
                </span>
        </div>

        <?php
        $facebook_custom = $integrationApi->getFacebookCustomisations($aiid);
        $page_greeting = isset($facebook_custom) ? $facebook_custom["page_greeting"] : "";
        $get_started_payload = isset($facebook_custom) ? $facebook_custom["get_started_payload"] : "";
        ?>

        <div class="row">
            <div class="col-md-12">
                <div class="form-group">
                    <label for="ai_name">Greeting message to display on the welcome screen</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="icon fa fa-comment-o"></i>
                        </div>
                        <input type="text" class="form-control flat no-shadow"
                               id="fb_page_greeting"
                               value="<?php echo htmlentities($page_greeting); ?>">
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="form-group">
                    <label for="ai_name">Command to send to the bot when the user taps the
                        Get Started button</label>
                    <div class="input-group">
                        <div class="input-group-addon">
                            <i class="icon fa fa-play-circle-o"></i>
                        </div>
                        <input type="text" class="form-control flat no-shadow"
                               id="fb_get_started_payload"
                               value="<?php echo htmlentities($get_started_payload); ?>">
                    </div>
                </div>
            </div>
        </div>

        <div style="padding-bottom: 20px">
            <button class="btn btn-primary flat center-block" id="fb-custom-save" style="width: 260px; display: none"
                    alt="save customisations">
                save customisations
            </button>
        </div>

        <div id="fb_messageus"></div>

        <div id="fb_sendtomessenger"></div>

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

<script>

    var message_us_data = {
        plugin_class: 'fb-messengermessageus',
        appid: '<?php echo $fb_app_id ?>',
        pageid: '<?php echo $facebook_state["page_integrated_id"]; ?>',
        button_name: 'Message Us',
        button_action: 'to start chatting to this bot on Facebook Messenger.',
        plugin_reference: 'https://developers.facebook.com/docs/messenger-platform/plugin-reference/message-us'
    };

    var sendtomessenger_data = {
            plugin_class: 'fb-send-to-messenger',
            appid: '<?php echo $fb_app_id ?>',
            pageid: '<?php echo $facebook_state["page_integrated_id"]; ?>',
            button_name: 'Send To Messenger',
            button_action: 'for this bot to start a conversation with you on Facebook Messenger.',
            plugin_reference: 'https://developers.facebook.com/docs/messenger-platform/plugin-reference/send-to-messenger',
            data_ref: 'RESPOND_TO_THIS'
        }
    ;

    $.get('./templates/integration_code.mustache', function (template) {
        $('#fb_messageus').replaceWith(Mustache.render(template, message_us_data));
        $('#fb_sendtomessenger').replaceWith(Mustache.render(template, sendtomessenger_data));
        // re-run facebook initialisation after templates have rendered
        window.fbAsyncInit();
    });

</script>