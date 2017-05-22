<?php
require "../pages/config.php";

require_once "../console/api/apiBase.php";
require_once "../console/api/intentsApi.php";
require_once "../console/api/aiApi.php";
require_once "../console/api/botstoreApi.php";

if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$intentsApi = new \hutoma\api\intentsApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

$intent_deleted = false;
if (isset($_REQUEST['deleteintent'])) {
    $intentName = $_REQUEST['deleteintent'];
    $result = $intentsApi->deleteIntent($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $intentName);
    $intent_deleted = true;
}

$intents = $intentsApi->getIntents($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
unset($intentsApi);

if ($intents['status']['code'] !== 200 && $intents['status']['code'] !== 404) {
    unset($intents);
    \hutoma\console::redirect('./error.php?err=210');
    exit;
}

$aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$bot= $aiApi->getSingleAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
unset($aiApi);

if ($bot['status']['code'] !== 200) {
    unset($bot);
    \hutoma\console::redirect('./error.php?err=204');
    exit;
}

function echoJsonIntentsResponse($intents)
{
    if ($intents['status']['code'] !== 404)
        echo json_encode($intents['intent_name']);
    else
        echo '""'; // return empty string
}

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | intents </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showIntents('')">
<?php include_once "../console/common/google_analytics.php"; ?>

<div class="wrapper">
    <header class="main-header">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <!-- ================ MENU CONSOLE ================= -->
    <aside class="main-sidebar ">
        <section class="sidebar">
            <p id="sidebarmenu"></p>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper" style="margin-right:350px;">
        <section class="content">
            <div class="row">
                <div class="col-md-12" id="intentElementBox">
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <?php include './dynamic/intent.content.create.html.php'; ?>
                    <?php include './dynamic/intent.content.list.html.php'; ?>
                </div>
            </div>
        </section>
    </div>

    <!-- ================ CHAT CONTENT ================= -->
    <aside class="control-sidebar control-sidebar-dark control-sidebar-open">
        <?php include './dynamic/chat.html.php'; ?>
        <?php include './dynamic/training.content.json.html.php'; ?>
    </aside>

    <footer class="main-footer" style="margin-right:350px;">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/intent/intent.polling.js"></script>
<script src="./scripts/intent/intent.js"></script>
<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>


<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "intents", 1, true, false]);
    </script>
</form>

<script>
    var intents = <?php echoJsonIntentsResponse($intents); unset($intents); ?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'intents_list';
    
    var intent_deleted = <?php if ($intent_deleted) echo 'true'; else echo 'false'; unset($intent_deleted)?>;
    var ai_state = <?php echo json_encode($bot['ai_status'])?>;
    var trainingFile = <?php if ($bot['training_file_uploaded']) echo 'true'; else echo 'false'; unset($bot)?>;
</script>
<script>
    function searchIntents(str) {
        showIntents(str);
    }
</script>
</body>
</html>