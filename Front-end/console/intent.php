<?php
    require "../pages/config.php";

    if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) {
        \hutoma\console::redirect('../pages/login.php');
        exit;
    }

    $intents = \hutoma\console::getIntents($_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['aiid']);

    if ($intents['status']['code'] !== 200 && $intents['status']['code'] !== 404) {
        unset($intents);
        \hutoma\console::redirect('./error.php?err=210');
        exit;
    }

    function echoJsonIntentsResponse($intents){
        if ( $intents['status']['code'] !== 404)
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
    <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">

</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini" onload="showIntents('')">
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
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-7">
                    <?php include './dynamic/intent.content.create.html.php'; ?>
                    <?php include './dynamic/intent.content.list.html.php'; ?>
                </div>
                <div class="col-md-5">
                    <?php include './dynamic/chat.html.php'; ?>
                    <?php include './dynamic/training.content.json.html.php'; ?>
                </div>
            </div>
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

<script src="./plugins/messaging/messaging.js"></script>
<script src="./plugins/validation/validation.js"></script>
<script src="./plugins/intent/intent.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>


<script src="./plugins/shared/shared.js"></script>
<script src="./plugins/sidebarMenu/sidebar.menu.js"></script>

<form action="" method="post" enctype="multipart/form-data">
    <script type="text/javascript">
        MENU.init([ "<?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?>","intents",1,true,false]);
    </script>
</form>

<script>
    var intents = <?php echoJsonIntentsResponse($intents); unset($intents); ?>;
    var newNode = document.createElement('div');
    newNode.className = 'row';
    newNode.id = 'intents_list';
</script>
<script>
    function searchIntents(str) {
        showIntents(str);
    }
</script>
</body>
</html>
