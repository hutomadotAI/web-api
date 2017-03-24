<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive()))
    \hutoma\console::redirect('../pages/login.php');

if (!isset($_POST['speech'])) {
    exit;
}
$_SESSION[$_SESSION['navigation_id']]['user_details']['speech'] = $_POST['speech'];
?>