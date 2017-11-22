<?php

namespace hutoma;

include_once __DIR__ . "/curl.php";

/**
 * Class curlHelper
 * Provides a leven of abstraction on top of PHP_curl
 * @package hutoma
 */
class apiConnector extends curl
{

    /**
     * Constructor.
     * @param $url - the Url
     * @param $devToken - the dev token
     */
    public function __construct($url = null, $devToken = null)
    {
        parent::__construct($url);


        if (isset($devToken)) {
            $this->setDevToken($devToken);
        }
    }

    /**
     * Sets the dev token
     * @param $devToken - the dev token
     */
    public function setDevToken($devToken)
    {
        $this->setCurlSecureOpt($devToken);
    }

    /**
     * Set the secure options for curl, including any custom security-related headers
     * @param $devToken - the dev token
     */
    private function setCurlSecureOpt($devToken)
    {
        $this->addHeader('Authorization', 'Bearer ' . $devToken);
        // Forces cURL to verify the peer's certificate - disabling this allows MITM attacks!
        $this->setOpt(CURLOPT_SSL_VERIFYPEER, true);
        // Check the existence of a common name and also verify that it matches the hostname provided
        $this->setOpt(CURLOPT_SSL_VERIFYHOST, 2);
    }

    /**
     * Destructor.
     */
    public function __destruct()
    {
        parent::__destruct();
    }

}