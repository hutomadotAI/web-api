<?php
namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/common/logging.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/botstoreApi.php";


logging::error(sprintf("%s - referrer: %s  errorObject: %s", $_GET['err'], $_SERVER['HTTP_REFERER'],
        isset($_GET['errObj']) ? $_GET['errObj'] : ""));


sessionObject::redirectToLoginIfUnauthenticated();
$header_page_title = "Error";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>

    <aside class="main-sidebar ">
        <section class="sidebar">
            <!-- ================ USER ACTION ================= -->
            <ul class="sidebar-menu disabled" style="position: absolute; bottom:0; width: 230px; min-height: 135px;">
                <li class="header" style="text-align: center;">MY ACCOUNT</li>
                <li><a href="./logout.php"><i class="fa fa-power-off text-red"></i> <span>LOGOUT</span></a></li>
            </ul>
        </section>
    </aside>

    <!-- ================ PAGE CONTENT ================= -->
    <div class="content-wrapper">
        <section class="content">
            <?php include __DIR__ . '/dynamic/error.content.html.php'; ?>
        </section>
    </div>

    <?php include __DIR__ . '/include/page_footer_default.php'; ?>
</div>

<script src="scripts/external/jQuery/jQuery-2.1.4.min.js"></script>
<script src="./bootstrap/js/bootstrap.min.js"></script>
<script src="scripts/external/slimScroll/jquery.slimscroll.min.js"></script>
<script src="scripts/external/fastclick/fastclick.min.js"></script>
<script src="./dist/js/app.min.js"></script>
</body>
</html>