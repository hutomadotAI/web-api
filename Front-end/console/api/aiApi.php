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
            $this->handleApiCallError($curl_response, 302);
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
            $this->handleApiCallError($curl_response, 303);
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
            $this->handleApiCallError($curl_response, 305);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return null;
    }

    public function updateAI($aiid, $description, $private, $language, $timezone, $personality, $voice, $confidence)
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
                'is_private' => $private == false ? 'false' : 'true',
                'personality' => $personality,
                'confidence' => $confidence,
                'voice' => $voice,
                'locale' => $locale,
                'timezone' => $timezone
            );

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 305);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function createAI($name, $description, $private, $language, $timezone, $confidence,
                             $personality, $voice, $contract, $payment_type, $price)
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
                'is_private' => $private == false ? 'false' : 'true',
                'personality' => $personality,
                'confidence' => $confidence,
                'voice' => $voice,
                'locale' => $locale,
                'timezone' => $timezone
            );

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, http_build_query($args));
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 301);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function chatAI($aiid, $chatId, $q, $history, $fs, $min_p, $topic)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/chat',
                array(
                    'q' => $q,
                    'chatId' => $chatId,
                    'chat_history' => $history,
                    'confidence_threshold' => $min_p,
                    'current_topic' => $topic
                )
            ));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 330);
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
            $this->handleApiCallError($curl_response, 360);
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
            $this->handleApiCallError($curl_response, 308);
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
            $this->handleApiCallError($curl_response, 309);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function uploadFile($aiid, $file, $source_type, $url)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/training',
                array('source_type' => $source_type)));

            $filename = $file['tmp_name'];
            $args['file'] = new \CurlFile($filename, 'text/plain', 'postfilename.txt');

            $this->curl->setVerbPost();
            $this->curl->setOpt(CURLOPT_POSTFIELDS, $args);
            $curl_response = $this->curl->exec();
            
            $this->handleApiCallError($curl_response, 350);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }

    public function addMesh($aiid, $aiid_mesh)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/mesh/' . $aiid_mesh));
            $this->curl->setVerbPost();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 001);
        }
        return true;
    }

    public function getMesh($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/mesh'));
            $this->curl->setVerbGet();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 001);
            $json_response = json_decode($curl_response, true);
            return $json_response;
        }
        return $this->getDefaultResponse();
    }


    public function deleteMesh($aiid, $aiid_mesh)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/mesh/' . $aiid_mesh));
            $this->curl->setVerbDelete();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 001);
        }
        return true;
    }

    public function deleteAllMesh($aiid)
    {
        if ($this->isLoggedIn()) {
            $this->curl->setUrl($this->buildRequestUrl(self::$path . '/' . $aiid . '/mesh'));
            $this->curl->setVerbDelete();
            $curl_response = $this->curl->exec();
            $this->handleApiCallError($curl_response, 001);
        }
        return true;
    }

    public function __destruct()
    {
        parent::__destruct();
    }
}