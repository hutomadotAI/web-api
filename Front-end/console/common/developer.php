<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 14/12/16
 * Time: 15:15
 */

namespace hutoma;


class developer
{
    private $name;
    private $email;
    private $address;
    private $postcode;
    private $city;
    private $country;
    private $website;
    private $company;
    
    public function __construct()
    {

    }

    public function setName($devName)
    {
        $this->name = $devName;
    }

    public function getName()
    {
        return $this->name;
    }


    public function setEmail($devEmail)
    {
        $this->email = $devEmail;
    }

    public function getEmail()
    {
        return $this->email;
    }


    public function setAddress($devAddress)
    {
        $this->address = $devAddress;
    }

    public function getAddress()
    {
        return $this->address;
    }


    public function setPostcode($devPostcode)
    {
        $this->postcode = $devPostcode;
    }

    public function getPostcode()
    {
        return $this->postcode;
    }


    public function setCity($devCity)
    {
        $this->city = $devCity;
    }

    public function getCity()
    {
        return $this->city;
    }


    public function setCountry($devCountry)
    {
        $this->country = $devCountry;
    }

    public function getCountry()
    {
        return $this->country;
    }


    public function setWebsite($devWebsite)
    {
        $this->website = $devWebsite;
    }

    public function getWebsite()
    {
        return $this->website;
    }


    public function setCompany($devCompany)
    {
        $this->company = $devCompany;
    }

    public function getCompany()
    {
        return $this->company;
    }


    public function __destruct()
    {

    }
}



