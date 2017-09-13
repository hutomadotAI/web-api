<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 27/10/16
 * Time: 09:10
 */

namespace hutoma\api;


class aiApi extends apiBase
{
    private static $path = "/ai";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function getAIs()
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getSingleAI($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function regenerateHmacSecretForAI($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/' . 'regenerate_webhook_secret'));
            $this->curl->setVerbPost();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function deleteAI($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid));
            $this->curl->setVerbDelete();
            $curl_response = $this->curl->exec();
            //$this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return null;
    }

    public function updateAI($aiid, $description, $language, $timezone, $personality, $voice, $confidence, $defaultChatResponses)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid));


            // TODO: remove hardcode depends how many language are supported
            $locales = array(
                'Deutsch' => 'de-DE',
                'Español' => 'es-ES',
                'Français' => 'fr-FR',
                'Italiano' => 'it-IT',
                'Nederlands' => 'nl-NL',
                'Português' => 'pt-PT',
                'English' => 'en-US'
            );

            if (array_key_exists($language, $locales)) {
                $locale = $locales[$language];
            } else {
                $locale = $locales['English'];
            }

            $args = array(
                'description' => $description,
                //TODO leave 'is_private' parameter until the backend remove it
                'is_private' => 'false',
                'personality' => $personality,
                'confidence' => $confidence,
                'voice' => $voice,
                'locale' => $locale,
                'timezone' => $timezone,
                // TODO: change to a true json array when we support multiple lines on the UI
                'default_chat_responses' => '["' . $defaultChatResponses . '"]'
            );

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function createAI($name, $description, $language, $timezone, $confidence,
                             $personality, $voice)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path));


            // TODO: remove hardcode depends how many language are supported
            $locales = array(
                'Deutsch' => 'de-DE',
                'Español' => 'es-ES',
                'Français' => 'fr-FR',
                'Italiano' => 'it-IT',
                'Nederlands' => 'nl-NL',
                'Português' => 'pt-PT',
                'English' => 'en-US'
            );

            if (array_key_exists($language, $locales)) {
                $locale = $locales[$language];
            } else {
                $locale = $locales['English'];
            }

            $args = array(
                'name' => $name,
                'description' => $description,
                //TODO leave 'is_private' parameter until the backend remove it
                'is_private' => 'false',
                'personality' => $personality,
                'confidence' => $confidence,
                'voice' => $voice,
                'locale' => $locale,
                'timezone' => $timezone
            );

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function exportAI($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/export'));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 500);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function chatAI($aiid, $chatId, $q)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/chat',
                array(
                    'q' => $q,
                    'chatId' => $chatId)
            ));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getDomains()
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/domain'));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function trainingStart($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/training/start'));
            $this->curl->setVerbPut();
            $curl_response = $this->curl->exec();
            //TODO - change handleApiCallError when ajax call are used -  $this->handleApiCallError($curl_response, 308);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return null;
    }

    public function trainingUpdate($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/training/update'));
            $this->curl->setVerbPut();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return null;
    }

    public function trainingStop($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/training/stop'));
            $this->curl->setVerbPut();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function uploadFile($aiid, $file, $source_type, $url)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/training', array('source_type' => $source_type)));

            $filename = $file['tmp_name'];
            $args['file'] = new \CurlFile($filename, 'text/plain', 'postfilename.txt');

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, $args);
            $curl_response = $this->curl->exec();

            //$this->handleApiCallError($curl_response, 350);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }


    public function getLinkedBots($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/bots'));
            $this->curl->setVerbGet();
            $this->curl->addHeader('Content-Type', 'application/json');
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }


    public function linkBotToAI($aiid, $botId)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/bot/' . $botId));
            $this->curl->setVerbPost();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function unlinkBotFromAI($aiid, $botId)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/bot/' . $botId));
            $this->curl->setVerbDelete();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function getIntegrations()
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/integration'));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}