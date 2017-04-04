<?php
require "../pages/config.php";

require_once "../console/api/apiBase.php";
require_once "../console/api/intentsApi.php";
require_once "../console/api/entityApi.php";
require_once "../console/api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive())) {
     exit;
}

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=118');
    exit;
}

if (isset($_POST['intent_name']))
    $intentName = $_POST['intent_name'];
else
    $intentName = $_POST['intent'];

$entityApi = new \hutoma\api\entityApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$entityList = $entityApi->getEntities();
unset($entityApi);

$intentsApi = new \hutoma\api\intentsApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$intent = $intentsApi->getIntent($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $intentName);
unset($intentsApi);

$aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$bot= $aiApi->getSingleAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
unset($aiApi);

if ($entityList['status']['code'] !== 200 && $entityList['status']['code'] !== 404) {
    unset($entityList);
    \hutoma\console::redirect('./error.php?err=210');
    exit;
}

if ($intent['status']['code'] !== 200 && $intent['status']['code'] !== 404) {
    unset($intent);
    \hutoma\console::redirect('./error.php?err=211');
    exit;
}

if ($bot['status']['code'] !== 200) {
    unset($bot);
    \hutoma\console::redirect('./error.php?err=204');
    exit;
}

function isPostInputAvailable()
{
    return (isset($_POST['intent']) || isset($_POST['intent_name']));
}

function echoJsonIntentResponse($intent)
{
    if ($intent['status']['code'] !== 404)
        echo json_encode($intent);
    else
        echo '""'; // return empty string
}

function echoJsonEntityListResponse($entityList)
{
    if ($entityList['status']['code'] !== 404) {
        echo json_encode($entityList['entity_name']);
    } else
        echo '""'; // return empty string
}

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Edit Intent </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
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
                    <?php include './dynamic/intent.element.content.head.html.php'; ?>
                    <?php include './dynamic/intent.element.content.expression.html.php'; ?>
                    <?php include './dynamic/intent.element.content.variable.html.php'; ?>
                    <?php include './dynamic/intent.element.content.response.html.php'; ?>
                    <?php include './dynamic/intent.element.content.webhook.html.php'; ?>
                    <?php include './dynamic/intent.element.content.prompt.html.php'; ?>
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

<script src="scripts/external/jQuery/jquery.omniselect.js"></script>
<script src="scripts/external/saveFile/FileSaver.js"></script>
<script src="./scripts/validation/validation.js"></script>
<script src="./scripts/button-select/buttonSelect.js"></script>
<script src="./scripts/intent/intent.polling.js"></script>
<script src="./scripts/intent/intent.element.response.js"></script>
<script src="./scripts/intent/intent.element.webhook.js"></script>
<script src="./scripts/intent/intent.element.expression.js"></script>
<script src="./scripts/intent/intent.element.js"></script>
<script src="./scripts/intent/intent.element.prompt.js"></script>
<script src="./scripts/intent/intent.element.variable.js"></script>

<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>

<script src="./scripts/messaging/messaging.js"></script>
<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>
<script src="scripts/external/saveFile/FileSaver.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "intents", 1, false, false]);
    </script>
</form>
<script>
    var entityListFromServer = <?php echo echoJsonEntityListResponse($entityList); unset($entityList);?>;
    var intent = <?php echoJsonIntentResponse($intent); unset($intent);?>;
    var trainingFile = <?php if ($bot['training_file_uploaded']) echo 'true'; else echo 'false'; unset($bot)?>;
    var data_changed = false;
</script>
</body>
</html>
