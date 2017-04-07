<?php

namespace hutoma;

include_once('bot.php');
include_once('developer.php');

class botstoreItem
{
    public $metadata;
    public $developer;
    private $order;
    private $owned;

    public function __construct()
    {
        $this->metadata = new bot();
        $this->developer = new developer();
    }

    public function getOrder()
    {
        return $this->order;
    }

    public function setOrder($order)
    {
        $this->order = $order;
    }

    public function getOwned()
    {
        return $this->owned;
    }

    public function setOwned($owned)
    {
        $this->owned = $owned;
    }

    public function toJSON()
    {
        $json = array(
            'metadata' => $this->metadata,
            'developer' => $this->developer,
            'order' => $this->getOrder(),
            'owned' => $this->getOwned()
        );
        return json_encode($json);
    }

    public static function fromObject($botstoreItem)
    {
        $botItem = new botstoreItem();
        $botItem->metadata = $botstoreItem['metadata'];
        $botItem->developer = $botstoreItem['developer'];
        $botItem->setOrder($botstoreItem['order']);
        $botItem->setOwned($botstoreItem['owned']);
        return $botItem;
    }

    public function __destruct()
    {

    }
}