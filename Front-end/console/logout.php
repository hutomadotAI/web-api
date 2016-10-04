<?php
require '../pages/config.php';
if((!\hutoma\console::$loggedIn)||(!\hutoma\console::isSessionActive())) \hutoma\console::redirect('../pages/login.php');

\hutoma\console::logout();
?>
