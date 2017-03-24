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
        if (isset($this->loggingUrl)) {
            $this->loggingUrl .= "/" . $this->getAppId() . "/log/_bulk";
        }
    }

    private function getAppId()
    {
        $appId = getenv("LOG_SERVICE_ANALYTICS_APPID");
        if (isset($appId) && $appId != "") {
            return $appId;
        } else {
            error_log("Telemetry - Could not obtain the APPID for this service");
        }
        return null;
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
            "dateTime" => date('c'),
            "type" => $type,
            "tag" => $tag,
            "message" => $message,
            "params" => $params
        );

        // Store it in case we can't upload right now
        array_push($GLOBALS["devconsole_telemetry"], $event);

        // Build bulk data
        $bulkData = "";
        foreach ($GLOBALS["devconsole_telemetry"] as $log) {
            $bulkData .= "{\"index\":{}}\n";
            $bulkData .= json_encode($log) . "\n";
        }

        // send to logging server
        $this->curl->setUrl($this->loggingUrl);
        //$this->curl->addHeader('Content-Type', 'application/json');
        $this->curl->addHeader('Content-Type', 'text/plain');
        $this->curl->post($bulkData);
        $response = $this->curl->exec();
        $code = $this->curl->getResultCode();
        if ($code == 200) {
            // clear the events
            unset($GLOBALS["devconsole_telemetry"]);
        } else {
            error_log("Failed to save telemetry events to server. Error code: " . $code . "  " . $response);
        }
    }
}