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
    private $postCode;
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


    public function setPostCode($devPostCode)
    {
        $this->postCode = $devPostCode;
    }

    public function getPostCode()
    {
        return $this->postCode;
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


    public function toJSON()
    {
        $json = array(
            'name' => $this->getName(),
            'email' => $this->getEmail(),
            'address' => $this->getAddress(),
            'postCode' => $this->getPostCode(),
            'city' => $this->getCity(),
            'country' => $this->getCountry(),
            'website' => $this->getWebsite(),
            'company' => $this->getCompany()
        );
        return json_encode($json);
    }


    public function __destruct()
    {

    }
}



