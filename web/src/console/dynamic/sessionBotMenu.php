<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive()))
   exit;

if (!isPostInputAvailable()) {
    echo json_encode(prepareResponse(500, 'Missing post data.'), true);
    exit;
}

$_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['botid'] = $_POST['botId'];
$_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['purchased'] = $_POST['purchased'];
$_SESSION[$_SESSION['navigation_id']]['user_details']['bot']['menu_title'] = $_POST['menu_title'];

\hutoma\console::redirect('../singlebotstore.php');

function isPostInputAvailable()
{
    return (
        isset($_POST['botId']) &&
        isset($_POST['purchased']) &&
        isset($_POST['menu_title'])
    );
}
?>