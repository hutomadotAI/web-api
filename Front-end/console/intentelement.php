<?php

    require "../pages/config.php";
    if (!isset($_POST['intent']) ) {
        header('Location: ./error.php?err=16');
        exit();
    }

    // fake request - we need to loading entity for a specific USER,AI, INTENT 
    $entityList = \hutoma\console::getIntegrations();


/*
    if ($entityList['status']['code'] !== 200) {
        unset($entityList);
        header('Location: ./error.php?err=3');
        exit;
    }
    */

?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>hu:toma | Create new Intent </title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/ionicons.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/hutoma-skin.css">
    <link rel="stylesheet" href="./plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet" href="./plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="./plugins/iCheck/skins/square/red.css">
    <link rel="stylesheet" href="./dist/css/AdminLTE.min.css">


    <link href="http://www.jqueryscript.net/css/jquerysctipttop.css" rel="stylesheet" type="text/css">
    <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">

</head>

<body class="hold-transition skin-blue-light fixed sidebar-mini">
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
                <li><a href="./home.php" tabindex="-1"><i class="fa fa-home text-light-blue" tabindex="-1"></i><span>home</span></a></li>
                <li class="active">
                    <a href="#" tabindex="-1">
                        <i class="fa fa-user text-olive" tabindex="-1" ></i><span><?php echo $_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['name']; ?></span><i class="fa fa-ellipsis-v pull-right"></i>
                    </a>
                    <ul class="treeview-menu">

                        <li><a href="./trainingAI.php" tabindex="-1"><i class="fa fa-graduation-cap text-purple" tabindex="-1"></i> <span>training</span></a></li>
                        <li class="active"><a href="./intent.php" tabindex="-1"><i class="fa fa-commenting-o text-green" tabindex="-1"></i><span>intents</span></a></li>
                        <li><a href="./entity.php"tabindex="-1" ><i class="fa fa-sitemap text-yellow" tabindex="-1"></i> <span>entities</span></a></li>
                        <li><a href="./domainsAI.php" tabindex="-1"><i class="fa fa-th text-red" tabindex="-1"></i> <span>domains</span></a></li>
                        <li><a href="./integrations.php" tabindex="-1" ><i class="glyphicon glyphicon-list-alt text-default" tabindex="-1"></i>integrations</a></li>
                        <li><a href="./settingsAI.php" tabindex="-1"><i class="fa fa-gear text-black" tabindex="-1"></i>settings</a></li>
                    </ul>
                </li>
                <li><a href="#" tabindex="-1"><i class="fa fa-book text-purple" tabindex="-1"></i> <span>Documentation</span></a></li>
            </ul>

            <ul class="sidebar-menu" style=" position: absolute; bottom:0; width: 230px; min-height: 135px;">
                <li class="header" style="text-align: center;">ACTION</li>
                <li><a href="#" tabindex="-1" ><i class="fa fa-shopping-cart text-green" style="position: relative;" tabindex="-1" ></i> <span>Marketplace</span></a></li>
                <li><a href="#" tabindex="-1" ><i class="fa fa-user text-blue" tabindex="-1" ></i> <span>Account</span></a></li>
                <li><a href="./logout.php" tabindex="-1" ><i class="fa fa-power-off text-red" tabindex="-1" ></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-8">
                    <?php include './dynamic/intent.element.content.head.html.php'; ?>
                    <?php include './dynamic/intent.element.content.expression.html.php'; ?>
                    <?php include './dynamic/intent.element.content.action.html.php'; ?>
                </div>
                <div class="col-md-4">
                    <?php include './dynamic/chat.html.php'; ?>
                    <?php include './dynamic/training.content.json.html.php'; ?>
                </div>
            </div>
        </section>
    </div>

    <!--
    <aside class="control-sidebar control-sidebar-dark">
    </aside>
    -->

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>
</div>

<script src="./plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="./plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="./plugins/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script src="./plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script src="./plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="./plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="./dist/js/demo.js"></script>
<script src="./plugins/iCheck/icheck.js"></script>
<script src="./plugins/intent/intent.element.js"></script>
<script src="./plugins/saveFile/FileSaver.js"></script>
<script src="./plugins/chat/chat.js"></script>
<script src="./plugins/chat/voice.js"></script>
<script src="./plugins/shared/shared.js"></script>

<script src="http://netdna.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
<script src="./plugins/jQuery/jquery.omniselect.js"></script>


<script>
    $(document).ready(function(){
        $('required').iCheck({
            checkboxClass: 'icheckbox_square',
            //increaseArea: '20%' // optional
        });
    });
    
    var entityList = <?php echo json_encode($entityList); unset($entityList);?>;
</script>

<script type="text/javascript">
    $(document).ready(function() {
        var $input = $('#states');

        $input.omniselect({
            source: ["Alabama","Alaska","Arizona","Arkansas","California","Colorado","Connecticut","Delaware","Florida","Georgia","Hawaii","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Dakota","North Carolina","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming"],
            resultsClass: 'typeahead dropdown-menu',
            activeClass: 'active',
            renderItem: function(label, id, index) {
                return '<li><a href="#">' + label + '</a></li>';
            }
        });

        $input.on('omniselect:select', function(event, value) {
            console.log('Selected: ' + value);
        });
    });
</script>
<script type="text/javascript">
    (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();
</script>


</body>
</html>
