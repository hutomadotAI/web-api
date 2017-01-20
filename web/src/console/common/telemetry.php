<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 24/11/16
 * Time: 11:05
 */

namespace hutoma;

include "TelemetryEvent.php";

class telemetry
{
    const APPID = "devconsole-v1";
    private static $instance;
    private $curl;
    private $loggingUrl;

    private function __construct()
    {
        $this->curl = new curl();
        if (!isset($GLOBALS["devconsole_telemetry"])) {
            $GLOBALS["devconsole_telemetry"] = array();
        }

        $envUrl = getenv("LOG_SERVICE_ANALYTICS_URL");
        if (isset($envUrl) && $envUrl != "") {
            $this->loggingUrl = $envUrl;
        } else {
            if (array_key_exists("logging", \hutoma\console::$config)) {
                if (array_key_exists("url", \hutoma\console::$config["logging"])) {
                    $this->loggingUrl = \hutoma\console::$config["logging"]["url"];
                }
            }
        }
    }

    public static function getInstance()
    {
        if (!isset(self::$instance)) {
            self::$instance = new telemetry();
        }
        return self::$instance;
    }


    public function log($type, $tag, $message, $params = null)
    {
        // Fail fast if we haven't defined the logging url
        if (!isset($this->loggingUrl)) {
            return;
        }

        $event = array(
            "timestamp" => round(microtime(true) * 1000), // need to convert to milliseconds
            "type" => $type,
            "tag" => $tag,
            "message" => $message,
            "params" => $params
        );

        // Store it in case we can't upload right now
        array_push($GLOBALS["devconsole_telemetry"], $event);

        // send to logging server
        $jsonArray = json_encode($GLOBALS["devconsole_telemetry"]);
        $this->curl->setUrl($this->loggingUrl . "?appId=" . self::APPID);
        $this->curl->addHeader('Content-Type', 'application/json');
        $this->curl->post($jsonArray);
        $this->curl->exec();
        $code = $this->curl->getResultCode();
        if ($code == 200) {
            // clear the events
            unset($GLOBALS["devconsole_telemetry"]);
        } else {
            error_log("Failed to save telemetry events to server. Error code: " . $code);
        }
    }


}