<?php

namespace hutoma;

require_once __DIR__ . "/common/globals.php";
require_once __DIR__ . "/common/sessionObject.php";
require_once __DIR__ . "/common/menuObj.php";
require_once __DIR__ . "/common/utils.php";
require_once __DIR__ . "/api/apiBase.php";
require_once __DIR__ . "/api/botstoreApi.php";
require_once __DIR__ . "/common/Assets.php";
require_once __DIR__ . "/dist/manifest.php";

$assets = new Assets($manifest);

sessionObject::redirectToLoginIfUnauthenticated();

$botId = isset($_REQUEST["botId"]) ? $_REQUEST["botId"] : null;
$category = isset($_REQUEST["category"]) ? $_REQUEST["category"] : null;

$aiName = sessionObject::getCurrentAI()['name'];
$isExistAiId = isset(sessionObject::getCurrentAI()['aiid']);

$header_page_title = "Botstore";
include __DIR__ . "/include/page_head_default.php";
include __DIR__ . "/include/page_body_default.php";
include __DIR__ . "/include/page_menu.php";
?>

<div class="wrapper">
    <?php include __DIR__ . "/include/page_header_default.php"; ?>


    <div class="content-wrapper">
        <section class="content">
            <div class="row">
                <div class="col-md-12">
                    <div class="box box-solid box-clean flat no-shadow unselectable" id="newAicontent">
                        <div class="box-header with-border">
                            <div class="box-title"><b>Hu:toma Botstore - transfer skills to your bot in few clicks</b>
                            </div>
                        </div>

                        <div class="box-body" id="boxNewAIBotstore">
                            <div class="alert alert-dismissable flat alert-info no-margin"
                                 id="containerMsgAlertNewAiBotstore"
                                 style="padding-bottom: 25px;">
                                <span id="msgAlertNewAiBotstore">
                                    <dd>
                                         The Hu:toma botstore allows you to purchase skills that you can then transfer to your bot. Mix and match bots together to create new functionalities. Bots purchased here will appear in the skill section when you <a
                                                href="newAI.php">create a new bot</a>.
                                         <br/>
                                         
                                    </dd>
                                </span>
                            </div>
                        </div>

                        <div class="box-footer">
                            <span>
                                If you’re stuck check out our <a data-toggle="collapse"
                                                                 href="#collapseCreateBotVideoTutorial">video tutorial</a> or email <a
                                        href='mailto:support@hutoma.ai?subject=Invite%20to%20slack%20channel'
                                        tabindex="-1">support@hutoma.ai</a> for an invite to our slack channel.
                            </span>
                            <p></p>

                            <div id="collapseCreateBotVideoTutorial" class="panel-collapse collapse">
                                <div class="box-body flat no-padding center-block"
                                     style="max-width: 700px;margin-auto;">
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
                        frameBorder="0"
                        scrolling="no"
                        height="0px"
                        id="contentFrame">
                </iframe>
            </div>
    </div>
    </section>
</div>

<?php include __DIR__ . '/include/page_footer_default.php'; ?>

<script src="/console/dist/vendors/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/console/dist/vendors/bootstrap/js/bootstrap.min.js"></script>
<script src="/console/dist/vendors/slimScroll/jquery.slimscroll.min.js"></script>
<script src="/console/dist/vendors/fastclick/fastclick.min.js"></script>
<script src="/console/dist/vendors/app.min.js"></script>
<script src="/console/dist/vendors/select2/select2.full.js"></script>
<script src="<? $assets->getAsset('botcard/botcard.js') ?>"></script>
<script src="<? $assets->getAsset('messaging/messaging.js') ?>"></script>
<script src="<? $assets->getAsset('shared/shared.js') ?>"></script>


<?php
$menuObj = new menuObj($aiName, $category, 2, false, !$isExistAiId);
include __DIR__ . "/include/page_menu_builder.php" ?>

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
                    document.getElementById('menu_' + removeSpecialCharacters(decodeURIComponent(e.data.category))).classList.add('active');
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