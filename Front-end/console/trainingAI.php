<?php
require "../pages/config.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

// If is it set, it means the user has selected a existing AI from home list
if (isset($_POST['ai']))
    CallGetSingleAI($_POST['ai']);

function CallGetSingleAI($aiid)
{
    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $singleAI = $aiApi->getSingleAI($aiid);
    unset($aiApi);
    if ($singleAI['status']['code'] === 200) {
        setSessionVariables($singleAI);
    } else {
        unset($singleAI);
        \hutoma\console::redirect('../error.php?err=200');
        exit;
    }
    unset($singleAI);
}

function setSessionVariables($singleAI)
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'] = $singleAI['aiid'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['client_token'] = $singleAI['client_token'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $singleAI['name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $singleAI['description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['created_on'] = $singleAI['created_on'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'] = $singleAI['is_private'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error'] = $singleAI['deep_learning_error'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_status'] = $singleAI['training_status'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status'] = $singleAI['ai_status'];

    // TO DO personality must be an integer value NOT boolean - for now is hard coded in false value
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $singleAI['personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $singleAI['confidence'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $singleAI['voice'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = localeToLanguage($singleAI['language']);
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $singleAI['timezone']['ID'];

    // TO DO getAiTrainingFile needs API call with response check before assigh the value
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['trainingfile'] = \hutoma\console::existsAiTrainingFile($singleAI['aiid']);
}

function localeToLanguage($locale)
{
    $languages = array(
        'de-DE' => 'Deutsch',
        'es-ES' => 'Español',
        'fr-FR' => 'Français',
        'it-IT' => 'Italiano',
        'nl-NL' => 'Nederlands',
        'pt-PT' => 'Português',
        'en-US' =>'English'
    );

    if (array_key_exists($locale, $languages)) {
        return $languages[$locale];
    } else {
        return $languages['en-US'];
    }
}

?>

<!DOCTYPE html>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | training AI</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="./plugins/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./plugins/iCheck/all.css">

</head>

<body class="hold-transition skin-blue fixed sidebar-mini" style="background-color: #2E3032;" id="trainingBody">

<script>
    var status = <?php echo json_encode(\hutoma\console::getAiStatus($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']));?>;
    var training_file = <?php echo json_encode(\hutoma\console::existsAiTrainingFile($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']));?>;
    var deep_error = <?php echo json_encode(\hutoma\console::getAiDeepLearningError($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']));?>;
</script>

<div class="wrapper">
    <header class="main-header" id="headerID">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <!-- ================ MENU CONSOLE ================= -->
    <aside class="main-sidebar ">
        <section class="sidebar">
            <p id="sidebarmenu"></p>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12" id="trainingBox">
                    <?php include './dynamic/training.content.info.html.php'; ?>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <?php include './dynamic/training.content.upload.html.php'; ?>
                    <?php include './dynamic/training.content.monitor.html.php'; ?>
                    <?php include './dynamic/training.content.keys.html.php'; ?>
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

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.js"></script>
<script src="./bootstrap/js/bootstrap-filestyle.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/iCheck/icheck.min.js"></script>
<script src="./plugins/training/training.area.upload.textfile.js"></script>
<script src="./plugins/training/training.area.upload.bookfile.js"></script>
<script src="./plugins/training/training.area.upload.urlfile.js"></script>
<script src="./plugins/training/training.area.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>
<script src="./plugins/clipboard/copyToClipboard.js"></script>


<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<script>
    var lang = <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language']); ?>;
    var voice = <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice']); ?>;
</script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "training", 1, true, false]);
    </script>
</form>

</body>
</html>
