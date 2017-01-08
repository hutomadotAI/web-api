<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 14/12/16
 * Time: 16:00
 */

namespace hutoma;

class bot
{
    private $activations;
    private $aiid;
    private $alertMessage;
    private $badge;
    private $category;
    private $classification;
    private $description;
    private $imagePath;
    private $licenseType;
    private $longDescription;
    private $name;
    private $price;
    private $privacyPolicy;
    private $rating;
    private $report;
    private $sample;
    private $update;
    private $users;
    private $version;
    private $videoLink;
    private $widgetColor;

    
    public function __construct()
    {

    }


    public function setActivations($botActivations)
    {
        $this->activations = $botActivations;
    }

    public function getActivations()
    {
        return $this->activations;
    }


    public function setAiid($aiid)
    {
        $this->aiid = $aiid;
    }

    public function getAiid()
    {
        return $this->aiid;
    }


    public function setAlertMessage($alertMessage)
    {
        $this->alertMessage = $alertMessage;
    }

    public function getAlertMessage()
    {
        return $this->alertMessage;
    }


    public function setBadge($badge)
    {
        $this->badge = $badge;
    }

    public function getBadge()
    {
        return $this->badge;
    }


    public function setCategory($category)
    {
        $this->category = $category;
    }

    public function getCategory()
    {
        return $this->category;
    }


    public function setClassification($classification)
    {
        $this->classification = $classification;
    }

    public function getClassification()
    {
        return $this->classification;
    }


    public function setDescription($description)
    {
        $this->description = $description;
    }

    public function getDescription()
    {
        return $this->description;
    }


    public function setImagePath($imagePath)
    {
        $this->imagePath = $imagePath;
    }

    public function getImagePath()
    {
        return $this->imagePath;
    }


    public function setLicenseType($licenseType)
    {
        $this->licenseType = $licenseType;
    }

    public function getLicenseType()
    {
        return $this->licenseType;
    }


    public function setLongDescription($longDescription)
    {
        $this->longDescription = $longDescription;
    }

    public function getLongDescription()
    {
        return $this->longDescription;
    }


    public function setName($name)
    {
        $this->name = $name;
    }

    public function getName()
    {
        return $this->name;
    }


    public function setPrice($price)
    {
        $this->price = $price;
    }

    public function getPrice()
    {
        return $this->price;
    }


    public function setPrivacyPolicy($privacyPolicy)
    {
        $this->privacyPolicy = $privacyPolicy;
    }

    public function getPrivacyPolicy()
    {
        return $this->privacyPolicy;
    }


    public function setRating($rating)
    {
        $this->rating = $rating;
    }

    public function getRating()
    {
        return $this->rating;
    }


    public function setReport($report)
    {
        $this->report = $report;
    }

    public function getReport()
    {
        return $this->report;
    }


    public function setSample($sample)
    {
        $this->sample = $sample;
    }

    public function getSample()
    {
        return $this->sample;
    }


    public function setUpdate($update)
    {
        $this->update = $update;
    }

    public function getUpdate()
    {
        return $this->update;
    }


    public function setUsers($users)
    {
        $this->users = $users;
    }

    public function getUsers()
    {
        return $this->users;
    }


    public function setVersion($version)
    {
        $this->version = $version;
    }

    public function getVersion()
    {
        return $this->version;
    }


    public function setVideoLink($videoLink)
    {
        $this->videoLink = $videoLink;
    }

    public function getVideoLink()
    {
        return $this->videoLink;
    }

    public function setWidgetColor($eidgetColor)
    {
        $this->widgetColor = $eidgetColor;
    }

    public function getWidgetColor()
    {
        return $this->widgetColor;
    }


    function licenceTypeToString($x){
        switch ($x) {
            case 0:
                return 'Trial';
            case 1:
                return 'Subscription';
            case 2:
                return 'Perpetual';
        }
    }

    function rangeActivation($n){
        switch (true) {
            case ($n < 10):
                return '0-10';
            case ($n < 100):
                return '10-100';
            case ($n < 1000):
                return '100-1000';
            case ($n < 5000):
                return '1.000-5.000';
            case ($n < 10000):
                return '5.000-10.000';
            case ($n < 20000):
                return '10.000-20.000';
            case ($n < 50000):
                return '20.000-50.000';
            case ($n < 100000):
                return '50.000-100.000';
            case ($n < 500000):
                return '100.000-500.000';
            case ($n < 1000000):
                return '500.000-1.000.000';
            case ($n < 5000000):
                return '1.000.000-5.000.000';
            case ($n < 10000000):
                return '5.000.000-10.000.000';
        }
    }

    public function toJSON()
    {

        $json = array(
            'activations' => $this->getActivations(),
            'aiid' => $this->getAiid(),
            'alertMessage' => $this->getAlertMessage(),
            'badge' => $this->getBadge(),
            'category' => $this->getCategory(),
            'classification' => $this->getClassification(),
            'description' => $this->getDescription(),
            'imagePath' => $this->getImagePath(),
            'licenceType' => $this->getLicenseType(),
            'longDescription' => $this->getLongDescription(),
            'name' => $this->getName(),
            'price' => $this->getPrice(),
            'privacyPolicy' => $this->getPrivacyPolicy(),
            'rating' => $this->getRating(),
            'report' => $this->getReport(),
            'sample' => $this->getSample(),
            'update' => $this->getUpdate(),
            'users' => $this->getUsers(),
            'version' => $this->getVersion(),
            'videoLink' => $this->getVideoLink(),
            'widgetColor' => $this->getWidgetColor()
        );
        return json_encode($json);
    }

    public function __destruct()
    {

    }
}



