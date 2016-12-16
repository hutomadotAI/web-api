<?php
require "../pages/config.php";
require_once "../console/common/bot.php";
require_once "../console/api/apiBase.php";
require_once "../console/api/aiApi.php";

if ((!\hutoma\console::$loggedIn) || (!\hutoma\console::isSessionActive())) {
    \hutoma\console::redirect('../pages/login.php');
    exit;
}

// If is it set, it means the user has selected a existing AI from home list
if (isset($_POST['ai']))
    CallGetSingleAI($_POST['ai']);


function CallGetSingleAI($aiid)
{
    $aiApi = new \hutoma\api\aiApi(\hutoma\console::isLoggedIn(), \hutoma\console::getDevToken());
    $singleAI = $aiApi->getSingleAI($aiid);
    unset($aiApi);
    if ($singleAI['status']['code'] === 200) {
        setSessionVariables($singleAI);
    } else {
        unset($singleAI);
        \hutoma\console::redirect('../error.php?err=200');
        exit;
    }
    unset($singleAI);
}

function setSessionVariables($singleAI)
{
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['aiid'] = $singleAI['aiid'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['client_token'] = $singleAI['client_token'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['name'] = $singleAI['name'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['description'] = $singleAI['description'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['created_on'] = $singleAI['created_on'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['private'] = $singleAI['is_private'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['deep_learning_error'] = $singleAI['deep_learning_error'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['training_status'] = $singleAI['training_status'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['status'] = $singleAI['ai_status'];

    // TO DO personality must be an integer value NOT boolean - for now is hard coded in false value
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['personality'] = $singleAI['personality'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['confidence'] = $singleAI['confidence'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['voice'] = $singleAI['voice'];
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['language'] = localeToLanguage($singleAI['language']);
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['timezone'] = $singleAI['timezone']['ID'];

    // TO DO getAiTrainingFile needs API call with response check before assigh the value
    $_SESSION[$_SESSION['navigation_id']]['user_details']['ai']['trainingfile'] = \hutoma\console::existsAiTrainingFile($singleAI['aiid']);
}

function localeToLanguage($locale)
{
    $languages = array(
        'de-DE' => 'Deutsch',
        'es-ES' => 'Español',
        'fr-FR' => 'Français',
        'it-IT' => 'Italiano',
        'nl-NL' => 'Nederlands',
        'pt-PT' => 'Português',
        'en-US' =>'English'
    );

    if (array_key_exists($locale, $languages)) {
        return $languages[$locale];
    } else {
        return $languages['en-US'];
    }
}

?>