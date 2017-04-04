<?php
require '../../pages/config.php';
require_once "../api/apiBase.php";
require_once "../api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::checkSessionIsActive()))
     exit;

if (!isPostInputAvailable()) {
    \hutoma\console::redirect('./error.php?err=106');
    exit;
}

$aiApi = new hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
$response = $aiApi->createAI(
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'],
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice']
);
unset($aiApi);


switch ($response['status']['code']) {
    case 200:
        $result = updateData($response['aiid']);
        if ($result == 200) {
            unset($_response);
            \hutoma\console::redirect('../trainingAI.php');
        } else {
            unset($_response);
            \hutoma\console::redirect('../error.php?err=15');
            exit;
        }
        break;
    case 400:
        \hutoma\console::redirect('../newAI.php?err=true');
        break;
    default:
        if (isset($response)) {
            \hutoma\console::redirect('../error.php?errObj=' . json_encode($response));
            unset($_response);
        } else {
            unset($_response);
            \hutoma\console::redirect('../error.php?err=16');
        }
        exit;
}


function isPostInputAvailable()
{
    return (
    isset($_POST['userActivedBots'])
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
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_status'] = $singleAI['training_status'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status'] = $singleAI['ai_status'];
        $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['client_token'] = $singleAI['client_token'];

        $json = $_POST['userActivedBots'];
        $botSkill = json_decode($json, true);

        foreach ($botSkill as $skill) {
            $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());

            if (!empty($skill['active'])) {

                if ($skill['active'] == '0')
                    $response = $aiApi->unlinkBotFromAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $skill['botId']);
                else
                    $response = $aiApi->linkBotToAI($_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'], $skill['botId']);

                if (!($response['status']['code'] == 200))
                    return $response['status']['code'];

            }
            unset($aiApi);
        }
        return 200;
    }
    return $singleAI['status']['code'];
}


function prepareResponse($code)
{
    $arr = array('status' => array('code' => $code));
    return $arr;
}


function UpdatelinkBotToAI($aiid,$botsLinked)
{

    foreach ($botsLinked as $bot) {
        $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
        if($bot['active']== '1') {
            $response = $aiApi->linkBotToAI($aiid, $bot['botId']);
            if($response['status']['code']!=200 && $response['status']['code']!=500 && $response['status']['code']!=404) {
                unset($response);
                unset($aiApi);
                return 0;
            }
        }
        unset($aiApi);
    }
    unset($response);
    return 1;
}


?>