<?php
/**
 * Created by IntelliJ IDEA.
 * User: pedrotei
 * Date: 24/11/16
 * Time: 11:41
 */

namespace hutoma;


class curl
{
    protected $curl;
    private $headers = array();

    /**
     * Constructor.
     * @param $url - the Url
     */
    public function __construct($url = null)
    {
        $this->curl = curl_init();

        if (isset($url)) {
            $this->setUrl($url);
        }
        curl_setopt($this->curl, CURLOPT_RETURNTRANSFER, true);
    }

    /**
     * Sets the url.
     * @param $url - the url
     */
    public function setUrl($url)
    {
        curl_setopt($this->curl, CURLOPT_URL, $url);
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
     * Sets the request verb to DELETE.
     */
    public function setVerbDelete()
    {
        $this->setOpt(CURLOPT_CUSTOMREQUEST, "DELETE");
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

    public function post($args)
    {
        $this->setVerbPost();
        $this->setOpt(CURLOPT_POSTFIELDS, $args);
    }

    /**
     * Sets the request verb to POST.
     */
    public function setVerbPost()
    {
        $this->setOpt(CURLOPT_CUSTOMREQUEST, NULL);
        $this->setOpt(CURLOPT_POST, true);
    }

    public function getResultCode()
    {
        return curl_getinfo($this->curl, CURLINFO_HTTP_CODE);
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