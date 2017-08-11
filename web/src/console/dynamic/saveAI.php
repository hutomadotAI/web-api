<?php

namespace hutoma;

require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/utils.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();

if (!isPostInputAvailable()) {
    utils::redirect('./error.php?err=106');
    exit;
}

$aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
$ai = sessionObject::getCurrentAI();
$response = $aiApi->createAI(
    $ai['name'],
    $ai['description'],
    $ai['language'],
    $ai['timezone'],
    $ai['confidence'],
    $ai['personality'],
    $ai['voice']
);
unset($aiApi);


switch ($response['status']['code']) {
    case 200:
        $result = updateData($response['aiid']);
        if ($result == 200) {
            unset($_response);
            utils::redirect('../trainingAI.php');
        } else {
            unset($_response);
            utils::redirect('../error.php?err=15');
            exit;
        }
        break;
    case 400:
        utils::redirect('../newAI.php?err=true&errObj='. urldecode($response), null);
        break;
    default:
        if (isset($response)) {
            utils::redirect('../error.php?errObj=' . urlencode($response), null);
            unset($_response);
        } else {
            unset($_response);
            utils::redirect('../error.php?err=16');
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
    $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
    $singleAI = $aiApi->getSingleAI($aiid);

    if ($singleAI['status']['code'] === 200) {
        sessionObject::populateCurrentAI($singleAI);

        $json = $_POST['userActivedBots'];
        $botSkill = json_decode($json, true);

        foreach ($botSkill as $skill) {
            if (!empty($skill['active'])) {
                if ($skill['active'] == '0')
                    $response = $aiApi->unlinkBotFromAI(sessionObject::getCurrentAI()['aiid'], $skill['botId']);
                else
                    $response = $aiApi->linkBotToAI(sessionObject::getCurrentAI()['aiid'], $skill['botId']);

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
        $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
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