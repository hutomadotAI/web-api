<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 28/01/17
 * Time: 22:29
 */

namespace hutoma;


class sessionObject
{

    public function getDevToken()
    {
        return $_SESSION['devToken'];
    }

    public function setDevToken($devToken)
    {
        $_SESSION['devToken'] = $devToken;
    }

    public function getLastActivity()
    {
        return $_SESSION['LAST_ACTIVITY'];
    }

    public function setLastActivity($lastActivity)
    {
        $_SESSION['LAST_ACTIVITY'] = $lastActivity;
    }
    
    public function clear() {
        $this->setDevToken(null);
        $this->setLastActivity(null);
    }
}