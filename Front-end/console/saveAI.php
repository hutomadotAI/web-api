<?php
    require '../pages/config.php';


    if ( !\hutoma\console::isSessionActive()) {
        header('Location: ./error.php?err=1');
        exit();
    }

    if ( !isValuesSessionInputFilled() ) {
        header("Location: ./error.php?err=2");
        exit();
    }

    $dev_token = \hutoma\console::getDevToken();
    $response = hutoma\console::createAI( $dev_token, $_SESSION['ai_name'], $_SESSION['ai_description'], $_SESSION['ai_language'], $_SESSION['ai_timezone'], $_SESSION['ai_confidence'], $_SESSION['ai_personality']);
    unset($dev_token);

    if ($response['status']['code'] === 200) {
        if (isset($_POST['userActivedDomains'])) {

            $userActivedList = json_decode($_POST['userActivedDomains'], true);

            $details = \hutoma\console::getUser();
            foreach ($userActivedList as $key => $value)
                \hutoma\console::insertUserActiveDomain($_SESSION['dev_id'] , $response['aiid'], $key, $userActivedList[$key]);

            $_SESSION['aiid'] = $response['aiid'];
            $_SESSION['ai_created_on'] = '';
            $_SESSION['ai_training_status'] = 0;
            $_SESSION['ai_deep_learning_error'] = 0.0;
            $_SESSION["ai_training_debug_info"] = '';
            $_SESSION['ai_training_status'] = 0;
            $_SESSION['ai_status'] = 0;
            //$_SESSION['ai_training_file'] = '';       // parameter missing
            $_SESSION['userActivedDomains'] = $_POST['userActivedDomains'];
            $_SESSION['current_ai_name'] = $_SESSION["ai_name"];
            unset($userActivedList);
        } else{

            unset($response);
            header('Location: ./error.php?err=4');
            exit();
        }
    }
    else{
        unset($dev_token);
        unset($response);
        header('Location: ./error.php?err=5');
        exit();
    }

    unset($dev_token);
    unset($response);

    //header('Location: trainingAI.php');


function isValuesSessionInputFilled(){
    return
        isset($_SESSION['ai_name']) &&
        isset($_SESSION['ai_description']) &&
        isset($_SESSION['ai_language']) &&
        isset($_SESSION['ai_timezone']) &&
        isset($_SESSION['ai_confidence']) &&
        isset($_SESSION['ai_personality']) &&
        isset($_SESSION['dev_id']);
}

?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Marketplace</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.min.css">
    <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">

    <link rel="stylesheet" href="./plugins/select2/select2.min.css">
    <link rel="stylesheet" href="./plugins/bootstrap-slider/slider.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="./plugins/ionslider/ion.rangeSlider.skinHTML5.css">
    <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
<div class="wrapper">

    <header class="main-header">
        <?php include './dynamic/header.html.php'; ?>
    </header>

    <aside class="main-sidebar ">
        <section class="sidebar">
            <!-- ================ USER PANEL ================== -->
            <?php include './dynamic/userpanel.html.php'; ?>
            <!-- ================ USER ACTION ================= -->
            <ul class="sidebar-menu">
                <li class="header">WORKPLACE</li>
                <li class="active"><a href="./home.php"><i class="fa fa-home"></i><span>home</span></a></li>
                <li><a href="#"><i class="fa fa-book"></i> <span>Documentation</span></a></li>
                <li class="header">ACTION</li>
                <li><a href="#"><i class="fa fa-shopping-cart text-green"></i> <span>Marketplace</span></a></li>
                <li><a href="#"><i class="fa fa-user text-blue"></i> <span>Account</span></a></li>
                <li><a href="#"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- =============================================== -->
    <!-- ================ PAGE CONTENT ================= -->
    <!-- =============================================== -->
    <div class="content-wrapper">
        <section class="content">
            <?php include './dynamic/marketplace.content.html.php'; ?>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <aside class="control-sidebar control-sidebar-dark">
        <?php include './dynamic/sidebar.controll.html.php'; ?>
    </aside>
</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./plugins/shared/shared.js"></script>

<script src="./plugins/select2/select2.full.min.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/createAI/createAI.js"></script>

</body>
</html>
