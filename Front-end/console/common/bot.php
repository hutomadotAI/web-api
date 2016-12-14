<?php
/**
 * Created by IntelliJ IDEA.
 * User: Hutoma
 * Date: 14/12/16
 * Time: 16:00
 */

namespace hutoma;

include "developer.php";

class bot
{
    private $name;
    private $description;
    private $longDescription;
    private $usecase;
    private $alarmMsg;

    private $iconPath;
    private $widgetColor;

    private $licenceType;
    private $licenceFee;

    private $classification;
    private $activations;
    private $rating;
    private $users;
    private $version;
    private $update;

    private $badge;
    private $badgeIcon;

    private $permissionLink;
    private $privacyLink;

    private $report;

    public $developer;
    
    public function __construct()
    {
        $this->developer = new developer();
    }

    public function setName($botName)
    {
        $this->name = $botName;
    }

    public function getName()
    {
        return $this->name;
    }


    public function setDescription($botDescription)
    {
        $this->description = $botDescription;
    }

    public function getDescription()
    {
        return $this->description;
    }


    public function setLongDescription($botLongDescription)
    {
        $this->longDescription = $botLongDescription;
    }

    public function getLongDescription()
    {
        return $this->longDescription;
    }


    public function setUsecase($botUsecase)
    {
        $this->usecase = $botUsecase;
    }

    public function getUsescase()
    {
        return $this->usecase;
    }


    public function setAlarmMessage($botAlarmMsg)
    {
        $this->alarmMsg = $botAlarmMsg;
    }

    public function getAlarmMessage()
    {
        return $this->alarmMsg;
    }


    public function setIconPath($botIconPath)
    {
        $this->iconPath = $botIconPath;
    }

    public function getIconPath()
    {
        return $this->iconPath;
    }


    public function setWidgetColor($botWidgetColor)
    {
        $this->widgetColor = $botWidgetColor;
    }

    public function getWidgetColor()
    {
        return $this->widgetColor;
    }


    public function setLicenceType($botLicenceType)
    {
        $this->licenceType = $botLicenceType;
    }

    public function getLicenceType()
    {
        return $this->licenceType;
    }


    public function setLicenceFee($botLicenceFee)
    {
        $this->licenceFee = $botLicenceFee;
    }

    public function getLicenceFee()
    {
        return $this->licenceFee;
    }


    public function setClassification($botClassification)
    {
        $this->classification = $botClassification;
    }

    public function getClassification()
    {
        return $this->classification;
    }


    public function setActivations($botActivations)
    {
        $this->activations = $botActivations;
    }

    public function getActivations()
    {
        return $this->activations;
    }


    public function setRating($botRating)
    {
        $this->rating = $botRating;
    }

    public function getRating()
    {
        return $this->rating;
    }


    public function setUsers($botUsers)
    {
        $this->users = $botUsers;
    }

    public function getUsers()
    {
        return $this->users;
    }

    public function setVersion($botVersion)
    {
        $this->version = $botVersion;
    }

    public function getVersion()
    {
        return $this->version;
    }


    public function setUpdate($botUpdate)
    {
        $this->update = $botUpdate;
    }

    public function getUpdate()
    {
        return $this->update;
    }


    public function setBadge($botBadge)
    {
        $this->badge = $botBadge;
    }

    public function getBadge()
    {
        return $this->badge;
    }


    public function setBadgeIcon($botBadgeIcon)
    {
        $this->badgeIcon = $botBadgeIcon;
    }

    public function getBadgeIcon()
    {
        return $this->badgeIcon;
    }


    public function setPermissionLink($botPermissionLink)
    {
        $this->permissionLink = $botPermissionLink;
    }

    public function getPermissionLink()
    {
        return $this->permissionLink;
    }


    public function setPrivacyLink($botPrivacyLink)
    {
        $this->privacyLink = $botPrivacyLink;
    }

    public function getPrivacyLink()
    {
        return $this->privacyLink;
    }


    public function setReport($botReport)
    {
        $this->report = $botReport;
    }

    public function getReport()
    {
        return $this->report;
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

    public function __destruct()
    {

    }
}



