<?php

namespace hutoma;

require_once __DIR__ . "/../common/errorRedirect.php";
require_once __DIR__ . "/../common/globals.php";
require_once __DIR__ . "/../common/sessionObject.php";
require_once __DIR__ . "/../common/ajaxApiProxy.php";
require_once __DIR__ . "/../api/apiBase.php";
require_once __DIR__ . "/../api/aiApi.php";

sessionObject::redirectToLoginIfUnauthenticated();


class aiProxy extends ajaxApiProxy {

    private function deleteAi($vars) {
        $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = $aiApi->deleteAI(sessionObject::getCurrentAI()['aiid']);
        unset($aiApi);
        utils::redirect('../home.php');
    }

    private function addAi($vars)
    {
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

        if (isset($response) && $response['status']['code'] === 200) {
            $aiid = $response['aiid'];
            $singleAI = $aiApi->getSingleAI($aiid);

            sessionObject::populateCurrentAI($singleAI);

            $json = $vars['userActivedBots'];
            $botSkills = json_decode($json, true);

            foreach ($botSkills as $skill) {
                if (!empty($skill['active'])) {
                    $shouldLink = $skill['active'] != '0';
                    if ($shouldLink) {
                        $response = $aiApi->linkBotToAI($aiid, $skill['botId']);
                    } else {
                        $response = $aiApi->unlinkBotFromAI($aiid, $skill['botId']);
                    }
                    if ($this->getApiReponseCode($response) !== 200) {
                        logging::error(sprintf(
                            "Error % skill: %s", $shouldLink ? "linking" : "unlinking", $response));
                        errorRedirect::handleErrorRedirect($response);
                        unset($aiApi);
                        return;
                    }
                }
            }
            unset($aiApi);
            utils::redirect('../trainingAI.php', null);
            return;
        }

        logging::error("Error creating AI");
        errorRedirect::handleErrorRedirect($response);
    }

    private function updateAi($vars) {
        $this->assertRequestVar($vars, 'aiid', 400, 'AIID not provided');
        $this->assertRequestVar($vars, 'description', 400, 'Description not provided');
        $this->assertRequestVar($vars, 'language', 400, 'Language not provided');
        $this->assertRequestVar($vars, 'timezone', 400, 'Timezone not provided');
        $this->assertRequestVar($vars, 'personality', 400, 'Personality not provided');
        $this->assertRequestVar($vars, 'voice', 400, 'Voice not provided');
        $this->assertRequestVar($vars, 'confidence', 400, 'Confidence not provided');
        $this->assertRequestVar($vars, 'default_chat_responses', 400, 'Default response not provided');
        $aiApi = new api\aiApi(sessionObject::isLoggedIn(), sessionObject::getDevToken());
        $result = $aiApi->updateAI(
            $vars['aiid'],
            $vars['description'],
            $vars['language'],
            $vars['timezone'],
            $vars['personality'],
            $vars['voice'],
            $vars['confidence'],
            $vars['default_chat_responses']
        );
        // Update the session variables
        sessionObject::getCurrentAI()['language'] = $vars['language'];
        sessionObject::getCurrentAI()['voice'] = $vars['voice'];

        unset($entityApi);
        echo json_encode($result);
    }

    public function onUpdate($vars) {
        $this->updateAi($vars);
    }

    public function onDelete($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }

    public function onAdd($vars) {
        // Need to delete through POST since HTML forms don't support CRUD
        if (isset($vars['action'])) {
            if ($vars['action'] === 'delete') {
                $this->deleteAi($vars);
            }
        } else {
            $this->addAi($vars);
        }
    }


    public function onGet($vars) {
        echo $this->buildResponse(400, 'Action not supported');
    }
}

$proxy = new aiProxy();
$proxy->handleRequest();
unset($proxy);

?>