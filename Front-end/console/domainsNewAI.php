<?php
require '../pages/config.php';

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=100');
    exit;
}

setSessionVariablesFromPost();
$domains = \hutoma\console::getBotsInStore();


//TODO replace me with API call once done
//if ($domains['status']['code'] !== 200) {
if ($domains === '') {
    unset($domains);
    \hutoma\console::redirect('./error.php?err=103');
    exit;
}

function isPostInputAvailable()
{
    return (
        isset($_POST['ai_name']) &&
        isset($_POST['ai_description']) &&
        isset($_POST['ai_language']) &&
        isset($_POST['ai_timezone']) &&
        isset($_POST['ai_confidence']) &&
        isset($_POST['ai_personality']) &&
        isset($_POST['ai_voice'])
    );
}

function setSessionVariablesFromPost()
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $_POST['ai_name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $_POST['ai_description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = $_POST['ai_language'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $_POST['ai_timezone'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $_POST['ai_confidence'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $_POST['ai_personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $_POST['ai_voice'];
    if (isset($_POST['ai_public']) && $_POST['ai_public'] == 'on')
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'] = 0;
    else
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'] = 1;
}

?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Pre-trained NN </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">

    <link rel="stylesheet" href="./plugins/iCheck/all.css">
    <link rel="stylesheet" href="./plugins/switch/switch.css">
    <link rel="stylesheet" href="./plugins/star/star.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini" onload="showDomains('',1)">
<div class="wrapper" id="wrapper">
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
    <div class="content-wrapper">
        <section class="content">
            <?php include './dynamic/domainsNewAI.content.html.php'; ?>
            <?php include './dynamic/botstore.content.info.details.html.php'; ?>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>

<script src="./plugins/domain/domain.js"></script>
<script src="./plugins/iCheck/icheck.min.js"></script>

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init(["", "home", 0, true, true]);
    </script>
</form>

<script>
    var domains = <?php echo json_encode($domains);  unset($domains); ?>;
    var userActived = {};
    for (var x in domains) {
        var key = domains[x].aiid;
        userActived[key] = false;
    }

    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'domains_list';
</script>

<script>
    function searchDomain(str) {
        showDomains(str, 1);
    }
</script>
</body>
</html>