<?php

namespace hutoma\api;

use hutoma\botstoreListParam;

define('CAROUSEL_CATEGORIES',  json_encode(array("Entertainment", "Education","Events", "Finance", "Fitness", "Health & Beauty",  "News", "Personal", "Other", "Social", "Travel", "Virtual Assistants")));
define('CAROUSEL_CATEGORIES_ICONS',  json_encode(array("fa-film", "fa-graduation-cap","fa-calendar-check-o", "fa-eur", "fa-bicycle", "fa-heartbeat", "fa-newspaper-o", "fa-male", "fa-search", "fa-thumbs-o-up", "fa-plane", "fa-headphones")));

include_once __DIR__ . "/../common/botstoreListParam.php";

class botstoreApi extends apiBase
{
    private static $UIEndpointPath = "/ui";
    private static $botstorePath = "/botstore";

    function __construct($sessionObject, $devToken)
    {
        parent::__construct($sessionObject, $devToken);
    }

    public function getBotstoreList(botstoreListParam $botstoreQuery)
    {
        $query = "startFrom=" . $botstoreQuery->getStartFrom() . "&";
        $query .= "pageSize=" . $botstoreQuery->getPageSize() . "&";

        $filters = $botstoreQuery->getFilters();
        if (isset($filters)) {
            foreach ($filters as $filter) {
                $query .= "filter=" . urlencode($filter) . "&";
            }
        }

        $orderField = $botstoreQuery->getOrderField();
        if (isset($orderField)) {
            $query .= "orderField=" . urlencode($orderField) . "&";
        }

        $orderDir = $botstoreQuery->getOrderDir();
        if (isset($orderDir)) {
            $query .= "orderDir=" . $orderDir;
        }

        $this->curl->setUrl($this->buildRequestUrl(self::$UIEndpointPath . self::$botstorePath) . '?' . $query);
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response;
    }

    public function getBotstoreListPerCategory(botstoreListParam $botstoreQuery)
    {
        $query = "max=" . $botstoreQuery->getPageSize() . "&";

        $this->curl->setUrl($this->buildRequestUrl(self::$UIEndpointPath . self::$botstorePath . '/per_category') . '?' . $query);
        $this->curl->setVerbGet();
        $curl_response = $this->curl->exec();
        $json_response = json_decode($curl_response, true);
        return $json_response;
    }

    public function getBotstoreBot($botId)
    {
        if (isset($botId)) {
            $this->curl->setUrl($this->buildRequestUrl(self::$UIEndpointPath . self::$botstorePath . '/' . $botId));
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