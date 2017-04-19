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

    public function addFilter($filterName, $filterValue)
    {
        array_push($this->filters, $filterName . "%3D%27" . $filterValue. "%27");
    }

    public function resetFilters()
    {
        //$this->filters = array();
    }

    public function getFilters()
    {
        return $this->filters;
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

    public function getQueryParameter()
    {
        $tmp_list_filters = array();
        foreach ($this->filters as $value) {
            array_push($tmp_list_filters,$value);
        }

        return array(
            'startFrom' => $this->getStartForm(),
            'pageSize' => $this->getPageSize(),
            'filter' => $tmp_list_filters,
            'orderField' => $this->getOrderFilter(),
            'orderDir' => $this->getOrderDir(),
        );
    }

    public function __destruct()
    {

    }
}