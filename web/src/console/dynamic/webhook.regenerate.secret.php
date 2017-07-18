<?php
/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: 18/07/17
 * Time: 08:43
 */

require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";
if(!\hutoma\console::checkSessionIsActive()){
    exit;
}

$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$status = $aiApi->regenerateHmacSecretForAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid']);
unset($aiApi);
echo json_encode($status, JSON_PRETTY_PRINT);
unset($status);

?>