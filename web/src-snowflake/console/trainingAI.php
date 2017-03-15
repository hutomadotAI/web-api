<?php
require "../pages/config.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'])) {
    \hutoma\console::redirect('./error.php?err=200');
    exit;
}

CallGetSingleAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);

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
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status'] = $singleAI['ai_status'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $singleAI['personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $singleAI['confidence'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $singleAI['voice'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = localeToLanguage($singleAI['language']);
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $singleAI['timezone'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['trainingfile'] = $singleAI['training_file_uploaded'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['phase_1_progress'] = $singleAI['phase_1_progress'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['phase_2_progress'] = $singleAI['phase_2_progress'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error'] = $singleAI['deep_learning_error'];
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
        'en-US' => 'English'
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
    <title>hu:toma | Training AI</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="scripts/external/iCheck/all.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" style="background-color: #2E3032;" id="trainingBody">
<?php include_once "../console/common/google_analytics.php"; ?>

<script>
    var deep_error = <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error']);?>;
    var aiStatus = {
        "ai_status": <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status']);?>,
        "phase_1_progress": <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['phase_1_progress']);?>,
        "phase_2_progress": <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['phase_2_progress']);?>,
        "deep_learning_error": deep_error,
        "training_file_uploaded": <?php echo json_encode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['trainingfile']);?>
    };

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
    <div class="content-wrapper" style="margin-right:350px;">
        <section class="content">

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
<script src="./bootstrap/js/bootstrap-filestyle.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./scripts/shared/shared.js"></script>
<script src="./scripts/messaging/messaging.js"></script>
<script src="scripts/external/iCheck/icheck.min.js"></script>
<script src="./scripts/chat/chat.js"></script>
<script src="./scripts/chat/voice.js"></script>
<script src="./scripts/clipboard/copyToClipboard.js"></script>

<script src="./scripts/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["<?php echo $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name']; ?>", "training", 1, true, false]);
    </script>
</form>

</body>
</html>
