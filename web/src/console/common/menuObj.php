<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 10/08/17
 * Time: 10:12
 */

namespace hutoma;


class menuObj
{
    public $aiName;
    public $topLevelHighlight;
    public $subMenuHighlight;
    public $limited = true;
    public $blockedClicked = false;

    public function __construct($aiName = "", $topLevelHighlight = "", $subMenuHighlight = 0, $blockedClicked = false, $limited = true)
    {
        $this->aiName = $aiName;
        $this->topLevelHighlight = $topLevelHighlight;
        $this->subMenuHighlight = $subMenuHighlight;
        $this->limited = $limited;
        $this->blockedClicked = $blockedClicked;
    }
}