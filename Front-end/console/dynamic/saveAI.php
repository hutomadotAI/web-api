<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive()))
    \hutoma\console::redirect('../pages/login.php');

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=106');
    exit;
}

$_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['userActivedDomains'] = $_POST['userActivedDomains'];

if (isPostSkipInputAvailable()) {
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['contract'] = $_POST['ai_contract'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['payment_type'] = $_POST['ai_payment_type'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['price'] = $_POST['ai_price'];
} else {
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['contract'] = 'skipped';
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['payment_type'] = 'skipped';
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['price'] = 'skipped';
}

$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $aiApi->createAI(
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['contract'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['payment_type'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['price']
);
unset($aiApi);

switch ($response['status']['code']) {
    case 200:
        if (updateData($response['aiid'])) {
            unset($_response);
            \hutoma\console::redirect('../trainingAI.php');
        } else {
            unset($_response);
            \hutoma\console::redirect('../error.php?err=15');
            exit;
        }
        break;
    case 400:
        \hutoma\console::redirect('../newAI.php?err=400');
        break;
    default:
        if (isset($response)) {
            \hutoma\console::redirect('../error.php?errObj=' . json_encode($response));
            unset($_response);
        } else {
            unset($_response);
            \hutoma\console::redirect('../error.php?err=15');
        }
        exit;
}

/*
    if ($response['status']['code'] === 200) {
        if (updateData($response['aiid'])) {
            unset($_response);
            \hutoma\console::redirect('../trainingAI.php');
        } else {
            unset($_response);
            \hutoma\console::redirect('./error.php?err=15');
            exit;
        }
    } else {
        if (isset($response)) {
            \hutoma\console::redirect('../error.php?errObj=' . json_encode($response));
            unset($_response);
        } else {
            unset($_response);
            \hutoma\console::redirect('./error.php?err=15');
        }
        exit;
    }
*/

function isPostInputAvailable()
{
    return (
    isset($_POST['userActivedDomains'])
    );
}

function isPostSkipInputAvailable()
{
    return (
        isset($_POST['ai_contract']) &&
        isset($_POST['ai_payment_type']) &&
        isset($_POST['ai_price'])
    );
}

function updateData($aiid)
{
    $aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $singleAI = $aiApi->getSingleAI($aiid);
    unset($aiApi);
    if ($singleAI['status']['code'] === 200) {

        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'] = $singleAI['aiid'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['created_on'] = $singleAI['created_on'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error'] = $singleAI['deep_learning_error'];
        //$_SESSION[ $_SESSION['navigation_id'] ]['user_details']['ai']['training_debug_info'] = $singleAI['ai']['training_debug_info'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_status'] = $singleAI['training_status'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status'] = $singleAI['ai_status'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['client_token'] = $singleAI['client_token'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['trainingfile'] = \hutoma\console::existsAiTrainingFile($singleAI['aiid']);


        if (storeAIMesh($singleAI['aiid'])) {
            unset($singleAI);
            return true;
        } else {
            unset($singleAI);
            return false;
        }

    }
}

function storeAIMesh($aiid)
{
    $aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $mesh = json_decode($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['userActivedDomains'], true);
    try {
        foreach ($mesh as $key => $value)
            if ($value) $aiApi->addMesh($aiid, $key);
    } catch (Exception $e) {
        unset($aiApi);
        unset($mesh);
        return false;
    }
    unset($mesh);
    unset($aiApi);
    return true;
}

?>