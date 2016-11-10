<?php

namespace hutoma;

/**
 * Class curlHelper
 * Provides a leven of abstraction on top of PHP_curl
 * @package hutoma
 */
class curlHelper
{
    private $curl;
    private $headers = array();

    /**
     * curlHelper constructor.
     * @param $url - the Url
     * @param $devToken - the dev token
     */
    public function __construct($url = null, $devToken = null)
    {
        $this->curl = curl_init();

        if (isset($url)) {
            $this->setUrl($url);
        }
        curl_setopt($this->curl, CURLOPT_RETURNTRANSFER, true);

        if (isset($devToken)) {
            $this->setDevToken($devToken);
        }
    }

    public function setUrl($url)
    {
        curl_setopt($this->curl, CURLOPT_URL, $url);
    }

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
     * Adds a header to the list of headers being sent with the request
     * @param $headerName - the header name
     * @param $headerValue - the header value
     */
    public function addHeader($headerName, $headerValue)
    {
        array_push($this->headers, $headerName . ": " . $headerValue);
    }

    /**
     * Sets a curl option.
     * @param $opt - the option name
     * @param $value - the option value
     */
    public function setOpt($opt, $value)
    {
        curl_setopt($this->curl, $opt, $value);
    }

    /**
     * Sets the request verb to DELETE.
     */
    public function setVerbDelete()
    {
        $this->setOpt(CURLOPT_CUSTOMREQUEST, "DELETE");
    }

    /**
     * Sets the request verb to GET.
     */
    public function setVerbGet()
    {
        $this->setOpt(CURLOPT_CUSTOMREQUEST, 'GET');
    }

    /**
     * Sets the request verb to PUT.
     */
    public function setVerbPut()
    {
        $this->setOpt(CURLOPT_CUSTOMREQUEST, 'PUT');
    }

    /**
     * Sets the request verb to POST.
     */
    public function setVerbPost()
    {
        $this->setOpt(CURLOPT_POST, true);
    }

    /**
     * Destructor.
     */
    public function __destruct()
    {
        $this->close();
    }

    /**
     * Closes the curl connection.
     */
    public function close()
    {
        if (isset($this->curl)) {
            curl_close($this->curl);
            unset($this->curl);
        }
    }

    public function getHeaders()
    {
        return $this->headers;
    }

    /**
     * Gets information about the last transfer.
     * @return mixed the info
     */
    public function getInfo()
    {
        return curl_getinfo($this->curl);
    }

    /**
     * Executes a request.
     * @return mixed the response for the request.
     */
    public function exec()
    {
        $this->setOpt(CURLOPT_HTTPHEADER, $this->headers);
        return curl_exec($this->curl);
    }
}