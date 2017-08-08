<?php
require "../pages/config.php";
require_once "api/apiBase.php";
require_once "api/botstoreApi.php";

if (!\hutoma\console::checkSessionIsActive()) {
    exit;
}

$botId = $_REQUEST["botId"];
$category = $_REQUEST["category"];

$aiName = $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'];
$isExistAiId = isset($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Hu:toma | Botstore</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="./bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="scripts/external/select2/select2.css">
    <link rel="stylesheet" href="./dist/css/font-awesome.min.css">
    <link rel="stylesheet" href="./dist/css/hutoma.css">
    <link rel="stylesheet" href="./dist/css/skins/skin-blue.css">
    <link rel="stylesheet" href="./scripts/switch/switch.css">
    <link rel="stylesheet" href="./scripts/star/star.css">
    <link rel="icon" href="dist/img/favicon.ico" type="image/x-icon">
    <script src="scripts/external/autopilot/autopilot.js"></script>
</head>

<body class="hold-transition skin-blue fixed sidebar-mini">
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
                <div class="col-md-12">
                    <div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
                        <div class="box-header with-border">
                            <div class="box-title"><b>Hu:toma Botstore - transfer skills to your AI in few clicks</b></div>
                        </div>

                        <div class="box-body" id="boxNewAIBotstore">
                            <div class="alert alert-dismissable flat alert-info no-margin" id="containerMsgAlertNewAiBotstore"
                                 style="padding-bottom: 25px;">
                                <span id="msgAlertNewAiBotstore">
                                    <dd>
                                         The Hu:toma botstore allows you to purchase skills that you can then transfer to your bot. Mix and match bots together to create new functionalities. Bots purchased here will appear in the skill section when you <a href="newAI.php">create a new AI</a>.
                                         <br/>
                                         
                                    </dd>
                                </span>
                           </div>
                        </div>

                        <div class="box-footer">
                            <span>
                                If you’re stuck check out our <a data-toggle="collapse" href="#collapseCreateBotVideoTutorial">video tutorial</a> or email <a href='mailto:support@hutoma.ai?subject=Invite%20to%20slack%20channel' tabindex="-1">support@hutoma.ai</a> for an invite to our slack channel.
                            </span>
                            <p></p>

                            <div id="collapseCreateBotVideoTutorial" class="panel-collapse collapse">
                                <div class="box-body flat no-padding center-block" style="max-width: 700px;margin-auto;">
                                    <div class="overlay center-block">
                                        <div class="embed-responsive embed-responsive-16by9" id="videoCreateBot">
                                            <iframe
                                                src="//www.youtube.com/embed/uFj73npjhbk?controls=1&hd=1&enablejsapi=1"
                                                frameborder="0" allowfullscreen>
                                            </iframe>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                </div>
                    <?php
                    $innerPage = "";
                    if (isset($botId)) {
                        $innerPage = "botcardDetail.php?botId=" . $botId . "&origin=botstore";
                    } else {
                        $innerPage = "botstoreList.php?category=" . urlencode($category);
                    }
                    ?>
                    <iframe src="<?php echo $innerPage ?>"
                            class="iframe-full-height"
                            frameBorder="0"
                            scrolling="no"
                            id="contentFrame">
                    </iframe>
                </div>
            </div>
        </section>
    </div>

    <footer class="main-footer">
        <?php include './dynamic/footer.inc.html.php'; ?>
    </footer>

    <script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
    <script src="./bootstrap/js/bootstrap.min.js"></script>
    <script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
    <script src="scripts/external/fastclick/fastclick.min.js"></script>
    <script src="./dist/js/app.min.js"></script>
    <script src="scripts/external/select2/select2.full.js"></script>

    <script src="./scripts/botstore/botstoreWizard.js"></script>
    <script src="./scripts/botcard/botcard.js"></script>

    <script src="./scripts/messaging/messaging.js"></script>
    <script src="./scripts/shared/shared.js"></script>

    <script src="./scripts/sidebarMenu/sidebar.menu.v2.js"></script>
    <form action="" method="post" enctype="multipart/form-data">
        <script type="text/javascript">
            MENU.init([
                "<?php echo $aiName;?>",
                "<?php echo $category;?>",
                2,
                false,
                <?php echo $isExistAiId ? "false" : "true" ?>
            ]);
        </script>
    </form>

    <script>
        window.addEventListener('message', function (e) {
            switch (e.data.event) {
                case 'BotstoreFinishPaintEvent':
                    var iFrame = document.getElementById('contentFrame');
                    iFrame.height = e.data.height + 'px';
                    iFrame.style.width = '100%';
                    break;
                case 'BotstoreCategoryChanged':
                    var menu = document.getElementById('botstoreMenu');
                    menu.childNodes.forEach(function (elem) {
                        elem.classList.remove('active');
                    });
                    document.getElementById('menu_' + removeSpecialCharacters(decodeURIComponent(e.detail.category))).classList.add('active');
                    break;
            }
        });

        <?php
        unset($aiName);
        unset($category);
        unset($isExistAiId);
        ?>
    </script>
</body>
</html>