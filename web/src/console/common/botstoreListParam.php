<?php

namespace hutoma;

class botstoreListParam
{
    private $startFrom;
    private $pageSize;
    private $filter;
    private $orderField;
    private $orderDir;

    public function __construct()
    {

    }

    public function getStartForm()
    {
        return $this->startFrom;
    }

    public function setStartForm($startFrom)
    {
        $this->startFrom = $startFrom;
    }

    public function getPageSize()
    {
        return $this->pageSize;
    }

    public function setPageSize($pageSize)
    {
        $this->pageSize = $pageSize;
    }

    public function getFilter()
    {
        return $this->filter;
    }

    public function setFilter($filter)
    {
        $this->filter = $filter;
    }

    public function getOrderFilter()
    {
        return $this->orderField;
    }

    public function setOrderFilter($orderField)
    {
        $this->orderField = $orderField;
    }

    public function getOrderDir()
    {
        return $this->orderDir;
    }

    public function setOrderDir($orderDir)
    {
        $this->orderDir = $orderDir;
    }

    public function toJSON()
    {
        $json = $this->toArray();
        return json_encode($json);
    }

    public static function fromObject($botstoreQueryParam)
    {
        $queryParam = new botstoreListParam();
        $queryParam->setStartForm($botstoreQueryParam['startFrom']);
        $queryParam->setPageSize($botstoreQueryParam['pageSize']);
        $queryParam->setFilter($botstoreQueryParam['filter']);
        $queryParam->setOrderFilter($botstoreQueryParam['orderField']);
        $queryParam->setOrderDir($botstoreQueryParam['orderDir']);
        return $queryParam;
    }

    public function toArray()
    {
        return array(
            'startFrom' => $this->getStartForm(),
            'pageSize' => $this->getPageSize(),
            'filter' => $this->getFilter(),
            'orderField' => $this->getOrderFilter(),
            'orderDir' => $this->getOrderDir()
        );
    }

    public function __destruct()
    {

    }
}