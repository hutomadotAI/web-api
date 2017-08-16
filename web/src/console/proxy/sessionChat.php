<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../api/apiBase.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isset($_POST['speech'])) {
    exit;
}
$_SESSION[$_SESSION['navigation_id']]['user_details']['speech'] = $_POST['speech'];
?>