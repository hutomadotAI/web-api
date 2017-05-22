<?php
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";
require_once "../common/utils.php";

header('P3P: CP="CAO PSA OUR"');
session_start();

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

if (isset($_COOKIE['logSyslogin'])) {
    $_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['botid'] = $_POST['botId'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['menu_title'] = $_POST['menu_title'];

    \hutoma\utils::redirect('../botcardDetail.php?botId=' . $_POST['botId'] . '&origin=' . $_POST['menu_title'] . '&PHPSESSID=" . $_COOKIE[\'PHPSESSID\']');

} else {
    \hutoma\utils::redirect('../botcardDetail.php?botId=' . $_POST['botId'] . '&PHPSESSID=" . $_COOKIE[\'PHPSESSID\']');
}


function isPostInputAvailable()
{
    return (
        isset($_POST['botId']) &&
        isset($_POST['menu_title'])
    );
}
?>