<?php

namespace hutoma;

class botstoreListParam
{
    private $startFrom;
    private $pageSize;
    private $filters = array();
    private $orderField;
    private $orderDir;

    public function __construct()
    {
        
    }

    public function getStartFrom()
    {
        return isset($this->startFrom) ? $this->startFrom : 0;
    }

    public function setStartForm($startFrom)
    {
        $this->startFrom = $startFrom;
    }

    public function getPageSize()
    {
        return isset($this->pageSize) ? $this->pageSize : 10;
    }

    public function setPageSize($pageSize)
    {
        $this->pageSize = $pageSize;
    }

    public function addFilter($filterName, $filterValue)
    {
        array_push($this->filters, $filterName . "='" . $filterValue. "'");
    }

    public function getFilters()
    {
        return $this->filters;
    }

    public function getOrderField()
    {
        return $this->orderField;
    }

    public function setOrderField($orderField)
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

    public function __destruct()
    {

    }
}