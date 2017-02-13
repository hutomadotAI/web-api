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
    const ICON_FOLDER = "dist/img/boticon/";
    const DEFAULT_ICON = "dist/img/default_bot.jpg";

    private $activations;
    private $aiid;
    private $alertMessage;
    private $badge;
    private $botId;
    private $category;
    private $classification;
    private $description;
    private $devId;
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
    private $iconFile;


    public function __construct()
    {

    }

    public static function fromObject($botDetails)
    {
        $theBot = new bot();
        $theBot->setAlertMessage($botDetails['alertMessage']);
        $theBot->setBadge($botDetails['badge']);
        $theBot->setBotId($botDetails['botId']);
        $theBot->setCategory($botDetails['category']);
        $theBot->setClassification($botDetails['classification']);
        $theBot->setDescription($botDetails['description']);
        $theBot->setLicenseType($botDetails['licenseType']);
        $theBot->setLongDescription($botDetails['longDescription']);
        $theBot->setName($botDetails['name']);
        $theBot->setPrice($botDetails['price']);
        $theBot->setPrivacyPolicy($botDetails['privacyPolicy']);
        $theBot->setSample($botDetails['sample']);
        $theBot->setVersion($botDetails['version']);
        $theBot->setVideoLink($botDetails['videoLink']);
        $theBot->setIconFile($botDetails['botIcon']);
        return $theBot;
    }

    public function getDevId()
    {
        return $this->devId;
    }

    public function setDevId($devId)
    {
        $this->devId = $devId;
    }

    function licenseTypeToString($x)
    {
        switch ($x) {
            case 0:
                return 'Trial';
            case 1:
                return 'Subscription';
            case 2:
                return 'Perpetual';
        }
    }

    function rangeActivation($n)
    {
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
            'aiid' => $this->getAiid(),
            'activations' => $this->getActivations(),
            'botId' => $this->getBotId(),
            'alertMessage' => $this->getAlertMessage(),
            'badge' => $this->getBadge(),
            'category' => $this->getCategory(),
            'classification' => $this->getClassification(),
            'description' => $this->getDescription(),
            'imagePath' => $this->getIconFile(),
            'licenseType' => $this->getLicenseType(),
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

    public function getAiid()
    {
        return $this->aiid;
    }

    public function setAiid($aiid)
    {
        $this->aiid = $aiid;
    }

    public function getActivations()
    {
        return $this->activations;
    }

    public function setActivations($botActivations)
    {
        $this->activations = $botActivations;
    }

    public function getBotId()
    {
        return $this->botId;
    }

    public function setBotId($botId)
    {
        $this->botId = $botId;
    }

    public function getAlertMessage()
    {
        return $this->alertMessage;
    }

    public function setAlertMessage($alertMessage)
    {
        $this->alertMessage = $alertMessage;
    }

    public function getBadge()
    {
        return $this->badge;
    }

    public function setBadge($badge)
    {
        $this->badge = $badge;
    }

    public function getCategory()
    {
        return $this->category;
    }

    public function setCategory($category)
    {
        $this->category = $category;
    }

    public function getClassification()
    {
        return $this->classification;
    }

    public function setClassification($classification)
    {
        $this->classification = $classification;
    }

    public function getDescription()
    {
        return $this->description;
    }

    public function setDescription($description)
    {
        $this->description = $description;
    }

    public function getIconFile()
    {
        if (!isset($this->iconFile)) {
            return self::DEFAULT_ICON;
        }
        return self::ICON_FOLDER . $this->iconFile;
    }

    public function setIconFile($iconFile)
    {
        $this->iconFile = $iconFile;
    }

    public function getLicenseType()
    {
        return $this->licenseType;
    }

    public function setLicenseType($licenseType)
    {
        $this->licenseType = $licenseType;
    }

    public function getLongDescription()
    {
        return $this->longDescription;
    }

    public function setLongDescription($longDescription)
    {
        $this->longDescription = $longDescription;
    }

    public function getName()
    {
        return $this->name;
    }

    public function setName($name)
    {
        $this->name = $name;
    }

    public function getPrice()
    {
        return $this->price;
    }

    public function setPrice($price)
    {
        $this->price = $price;
    }

    public function getPrivacyPolicy()
    {
        return $this->privacyPolicy;
    }

    public function setPrivacyPolicy($privacyPolicy)
    {
        $this->privacyPolicy = $privacyPolicy;
    }

    public function getRating()
    {
        return $this->rating;
    }

    public function setRating($rating)
    {
        $this->rating = $rating;
    }

    public function getReport()
    {
        return $this->report;
    }

    public function setReport($report)
    {
        $this->report = $report;
    }

    public function getSample()
    {
        return $this->sample;
    }

    public function setSample($sample)
    {
        $this->sample = $sample;
    }

    public function getUpdate()
    {
        return $this->update;
    }

    public function setUpdate($update)
    {
        $this->update = $update;
    }

    public function getUsers()
    {
        return $this->users;
    }

    public function setUsers($users)
    {
        $this->users = $users;
    }

    public function getVersion()
    {
        return $this->version;
    }

    public function setVersion($version)
    {
        $this->version = $version;
    }

    public function getVideoLink()
    {
        return $this->videoLink;
    }

    public function setVideoLink($videoLink)
    {
        $this->videoLink = $videoLink;
    }

    public function getWidgetColor()
    {
        return $this->widgetColor;
    }

    public function setWidgetColor($widgetColor)
    {
        $this->widgetColor = $widgetColor;
    }

    public function __destruct()
    {

    }
}



