CREATE DATABASE  IF NOT EXISTS `hutoma` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `hutoma`;
-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: hutoma
-- ------------------------------------------------------
-- Server version	5.7.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai`
--

DROP TABLE IF EXISTS `ai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ai` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `aiid` varchar(50) NOT NULL,
  `ai_name` varchar(50) DEFAULT NULL,
  `ai_description` varchar(250) DEFAULT NULL,
  `default_chat_responses` JSON NOT NULL,
  `created_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `dev_id` varchar(50) NOT NULL,
  `is_private` tinyint(1) DEFAULT '1',
  `client_token` varchar(250) NOT NULL,
  `hmac_secret` varchar(50) DEFAULT NULL,
  `ui_ai_language` varchar(10) DEFAULT 'en-US',
  `ui_ai_timezone` varchar(50) DEFAULT 'UTC',
  `ui_ai_confidence` double DEFAULT NULL,
  `ui_ai_personality` tinyint(4) DEFAULT '0',
  `ui_ai_voice` int(11) DEFAULT '0',
  `deleted` tinyint(1) DEFAULT '0',
  `passthrough_url` varchar(2048) DEFAULT NULL,
  `api_keys_desc` JSON DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `aiid_UNIQUE` (`aiid`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_integration`
--

DROP TABLE IF EXISTS `ai_integration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ai_integration` (
  `aiid` varchar(50) NOT NULL,
  `integration` varchar(50) NOT NULL,
  `integrated_resource` varchar(250) DEFAULT NULL,
  `integrated_userid` varchar(250) DEFAULT NULL,
  `data` json DEFAULT NULL,
  `status` varchar(1024) DEFAULT NULL,
  `active` tinyint(4) DEFAULT '0',
  `chat_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`aiid`,`integration`),
  KEY `integration` (`integration`,`integrated_resource`),
  CONSTRAINT `fk_integration_aiid` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_memory`
--

DROP TABLE IF EXISTS `ai_memory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ai_memory` (
  `aiid` varchar(50) NOT NULL,
  `uid` varchar(50) NOT NULL,
  `variable_name` varchar(50) NOT NULL,
  `variable_value` varchar(500) NOT NULL,
  `last_accessed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_seconds` int(11) NOT NULL,
  `n_prompts` int(11) NOT NULL,
  `variable_type` varchar(20) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  PRIMARY KEY (`aiid`,`uid`,`variable_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_status`
--

DROP TABLE IF EXISTS `ai_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ai_status` (
  `server_type` varchar(10) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `training_status` varchar(45) NOT NULL,
  `training_progress` float DEFAULT '0',
  `training_error` float DEFAULT '10000',
  `server_endpoint` varchar(256) NOT NULL,
  `queue_action` varchar(50) DEFAULT NULL,
  `queue_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`server_type`,`aiid`),
  KEY `fk_aiid` (`aiid`),
  KEY `idx_ai_status_training_status` (`training_status`),
  CONSTRAINT `fk_aiid` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_training`
--

DROP TABLE IF EXISTS `ai_training`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ai_training` (
  `aiid` varchar(50) NOT NULL,
  `ai_trainingfile` longtext,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`aiid`),
  CONSTRAINT `aiid_ibfk_1` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `api_rate_limit`
--

DROP TABLE IF EXISTS `api_rate_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `api_rate_limit` (
  `dev_id` varchar(50) NOT NULL,
  `rate_key` varchar(50) NOT NULL,
  `tokens` float DEFAULT NULL,
  `token_update_time` bigint(20) DEFAULT NULL,
  `expires` datetime DEFAULT NULL,
  PRIMARY KEY (`dev_id`,`rate_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `botIcon`
--

DROP TABLE IF EXISTS `botIcon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `botIcon` (
  `botId` int(11) NOT NULL,
  `icon` mediumblob NOT NULL,
  PRIMARY KEY (`botId`),
  CONSTRAINT `botIcon_ibfk_1` FOREIGN KEY (`botId`) REFERENCES `botStore` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `botPurchase`
--

DROP TABLE IF EXISTS `botPurchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `botPurchase` (
  `botId` int(11) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `purchase_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `price` decimal(10,0) NOT NULL,
  PRIMARY KEY (`botId`,`dev_id`),
  KEY `dev_id` (`dev_id`),
  CONSTRAINT `botPurchase_ibfk_1` FOREIGN KEY (`botId`) REFERENCES `botStore` (`id`) ON DELETE CASCADE,
  CONSTRAINT `botPurchase_ibfk_2` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `botStore`
--

DROP TABLE IF EXISTS `botStore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `botStore` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dev_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(1024) NOT NULL,
  `long_description` text,
  `alert_message` varchar(150) DEFAULT NULL,
  `badge` varchar(20) DEFAULT NULL,
  `license_type` varchar(50) NOT NULL,
  `price` decimal(10,0) NOT NULL,
  `sample` text,
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `category` varchar(50) NOT NULL,
  `privacy_policy` text,
  `classification` varchar(50) NOT NULL,
  `version` varchar(25) NOT NULL,
  `video_link` varchar(1800) DEFAULT NULL,
  `publishing_state` tinyint(1) NOT NULL,
  `publishing_type` tinyint(1) NOT NULL DEFAULT '1',
  `botIcon` varchar(255) DEFAULT NULL,
  `featured` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `dev_id` (`dev_id`),
  KEY `aiid` (`aiid`),
  KEY `idx_botStore_publishing_state_publishing_type` (`publishing_state`,`publishing_type`),
  CONSTRAINT `botStore_ibfk_1` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE,
  CONSTRAINT `botStore_ibfk_2` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bot_ai`
--

DROP TABLE IF EXISTS `botTemplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `botTemplate` (
  `botId` INT NOT NULL,
  `template` LONGTEXT NULL,
  UNIQUE INDEX `botId_UNIQUE` (`botId` ASC),
  PRIMARY KEY (`botId`),
  CONSTRAINT `fk_botTemplate_botId`
    FOREIGN KEY (`botId`)
    REFERENCES `hutoma`.`botStore` (`id`)
    ON DELETE CASCADE
  ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bot_ai`
--

DROP TABLE IF EXISTS `bot_ai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bot_ai` (
  `botId` int(11) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  PRIMARY KEY (`botId`,`aiid`,`dev_id`),
  KEY `aiid` (`aiid`),
  KEY `dev_id` (`dev_id`),
  CONSTRAINT `bot_ai_ibfk_1` FOREIGN KEY (`botId`) REFERENCES `botStore` (`id`) ON DELETE CASCADE,
  CONSTRAINT `bot_ai_ibfk_2` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE,
  CONSTRAINT `bot_ai_ibfk_3` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bot_ai_config`
--
DROP TABLE IF EXISTS `bot_ai_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bot_ai_config` (
  `dev_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `botId` int(11) NOT NULL,
  `config` JSON NULL,
  PRIMARY KEY (`dev_id`, `aiid`, `botId`),
  KEY `aiid` (`aiid`),
  KEY `dev_id` (`dev_id`),
  KEY `botId` (`botId`),
  CONSTRAINT `bot_ai_config_ibfk_1` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE,
  CONSTRAINT `bot_ai_config_ibfk_2` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `chatState`
--

DROP TABLE IF EXISTS `chatState`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chatState` (
  `dev_id` varchar(50) NOT NULL,
  `chat_id` varchar(50) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `topic` varchar(250) DEFAULT NULL,
  `history` varchar(1024) DEFAULT NULL,
  `locked_aiid` varchar(50) DEFAULT NULL,
  `entity_values` text,
  `confidence_threshold` DOUBLE DEFAULT NULL,
  PRIMARY KEY (`dev_id`,`chat_id`),
  UNIQUE KEY `chat_id_UNIQUE` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `controller_state`
--

DROP TABLE IF EXISTS `controller_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `controller_state` (
  `server_type` varchar(10) NOT NULL,
  `verified_server_count` int(11) DEFAULT NULL,
  `training_capacity` int(11) DEFAULT NULL,
  `training_slots_available` int(11) DEFAULT NULL,
  `chat_capacity` int(11) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`server_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `debug`
--

DROP TABLE IF EXISTS `debug`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `debug` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `developerInfo`
--

DROP TABLE IF EXISTS `developerInfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `developerInfo` (
  `dev_id` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `company` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `address` varchar(200) NOT NULL,
  `post_code` varchar(100) NOT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `website` varchar(1024) NOT NULL,
  PRIMARY KEY (`dev_id`),
  CONSTRAINT `developerInfo_ibfk_1` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `devplan`
--

DROP TABLE IF EXISTS `devplan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `devplan` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `maxai` int(11) NOT NULL,
  `monthlycalls` int(11) NOT NULL,
  `maxmem` int(11) NOT NULL,
  `maxtraining` int(11) NOT NULL DEFAULT '0',
  `plan_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `domains`
--

DROP TABLE IF EXISTS `domains`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domains` (
  `dom_id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(250) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `color` varchar(50) NOT NULL,
  `available` tinyint(1) NOT NULL,
  PRIMARY KEY (`dom_id`),
  UNIQUE KEY `id` (`dom_id`),
  KEY `id_2` (`dom_id`),
  KEY `id_3` (`dom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity`
--

DROP TABLE IF EXISTS `entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dev_id` varchar(50) NOT NULL,
  `name` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isSystem` tinyint(1) NOT NULL DEFAULT '0',
  `isPersistent` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dev_id` (`dev_id`,`name`),
  CONSTRAINT `entity_ibfk_1` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_value`
--

DROP TABLE IF EXISTS `entity_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entity_id` int(11) NOT NULL,
  `value` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `entity_id` (`entity_id`,`value`),
  CONSTRAINT `entity_value_ibfk_1` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integrations`
--

DROP TABLE IF EXISTS `integrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `integrations` (
  `int_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(150) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `available` tinyint(1) NOT NULL,
  PRIMARY KEY (`int_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent`
--

DROP TABLE IF EXISTS `intent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `aiid` varchar(50) NOT NULL,
  `name` varchar(250) NOT NULL,
  `topic_in` varchar(250) DEFAULT NULL,
  `topic_out` varchar(250) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `aiid` (`aiid`,`name`),
  CONSTRAINT `intent_ibfk_1` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent_response`
--

DROP TABLE IF EXISTS `intent_response`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent_response` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_id` int(11) NOT NULL,
  `response` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_id` (`intent_id`,`response`),
  CONSTRAINT `intent_response_ibfk_1` FOREIGN KEY (`intent_id`) REFERENCES `intent` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent_user_says`
--

DROP TABLE IF EXISTS `intent_user_says`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent_user_says` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_id` int(11) NOT NULL,
  `says` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_id` (`intent_id`,`says`),
  CONSTRAINT `intent_user_says_ibfk_1` FOREIGN KEY (`intent_id`) REFERENCES `intent` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent_variable`
--

DROP TABLE IF EXISTS `intent_variable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent_variable` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_id` int(11) NOT NULL,
  `entity_id` int(11) NOT NULL,
  `required` int(1) DEFAULT '0',
  `n_prompts` int(11) DEFAULT '3',
  `value` varchar(250) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dummy` int(1) DEFAULT '0',
  `label` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `intent_id` (`intent_id`),
  KEY `entity_id` (`entity_id`),
  CONSTRAINT `intent_variable_ibfk_1` FOREIGN KEY (`intent_id`) REFERENCES `intent` (`id`) ON DELETE CASCADE,
  CONSTRAINT `intent_variable_ibfk_2` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent_variable_prompt`
--

DROP TABLE IF EXISTS `intent_variable_prompt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent_variable_prompt` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_variable_id` int(11) NOT NULL,
  `prompt` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_variable_id` (`intent_variable_id`,`prompt`),
  CONSTRAINT `intent_variable_prompt_ibfk_1` FOREIGN KEY (`intent_variable_id`) REFERENCES `intent_variable` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invite_code_uses`
--

DROP TABLE IF EXISTS `invite_code_uses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `invite_code_uses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `invite_code` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invite_codes`
--

DROP TABLE IF EXISTS `invite_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `invite_codes` (
  `code` varchar(50) NOT NULL,
  `remaining_uses` int(11) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `memoryIntent`
--

DROP TABLE IF EXISTS `memoryIntent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `memoryIntent` (
  `aiid` varchar(50) NOT NULL,
  `chatId` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `variables` mediumtext NOT NULL,
  `lastAccess` datetime NOT NULL,
  `isFulfilled` tinyint(1) NOT NULL,
  PRIMARY KEY (`aiid`,`chatId`),
  UNIQUE KEY `idx_memoryIntent_aiid_chatId_name` (`aiid`,`chatId`,`name`),
  KEY `idx_memoryIntent_lastAccess` (`lastAccess`),
  KEY `idx_aiid` (`aiid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resetTokens`
--

DROP TABLE IF EXISTS `resetTokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resetTokens` (
  `token` varchar(40) NOT NULL COMMENT 'The Unique Token Generated',
  `uid` int(11) NOT NULL COMMENT 'The User Id',
  `requested` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userAIDomains`
--

DROP TABLE IF EXISTS `userAIDomains`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userAIDomains` (
  `dev_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `dom_id` varchar(50) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `created_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dev_id`,`aiid`,`dom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(64) NOT NULL,
  `password_salt` varchar(250) NOT NULL,
  `first_name` varchar(30) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `attempt` varchar(15) NOT NULL DEFAULT '0',
  `dev_token` varchar(250) NOT NULL,
  `plan_id` int(11) NOT NULL DEFAULT '0',
  `dev_id` varchar(50) NOT NULL,
  `last_name` varchar(30) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `valid` tinyint(1) NOT NULL DEFAULT '1',
  `internal` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dev_id_UNIQUE` (`dev_id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `webhooks`
--

DROP TABLE IF EXISTS `webhooks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `webhooks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `aiid` varchar(50) NOT NULL,
  `intent_name` varchar(250) NOT NULL,
  `endpoint` varchar(2048) NOT NULL,
  `enabled` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'hutoma'
--
/*!50106 SET @save_time_zone= @@TIME_ZONE */ ;
/*!50106 DROP EVENT IF EXISTS `clean_rate_limit` */;
DELIMITER ;;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;;
/*!50003 SET character_set_client  = latin1 */ ;;
/*!50003 SET character_set_results = latin1 */ ;;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;;
/*!50003 SET sql_mode              = '' */ ;;
/*!50003 SET @saved_time_zone      = @@time_zone */ ;;
/*!50003 SET time_zone             = 'SYSTEM' */ ;;
/*!50106 CREATE*/ /*!50117 DEFINER=`admin`@`127.0.0.1`*/ /*!50106 EVENT `clean_rate_limit` ON SCHEDULE EVERY 1 DAY STARTS '2016-09-14 13:50:06' ON COMPLETION PRESERVE ENABLE DO BEGIN

  DELETE FROM  `api_rate_limit` WHERE expires < NOW( );

END */ ;;
/*!50003 SET time_zone             = @saved_time_zone */ ;;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;;
/*!50003 SET character_set_client  = @saved_cs_client */ ;;
/*!50003 SET character_set_results = @saved_cs_results */ ;;
/*!50003 SET collation_connection  = @saved_col_connection */ ;;
DELIMITER ;
/*!50106 SET TIME_ZONE= @save_time_zone */ ;

--
-- Dumping routines for database 'hutoma'
--
/*!50003 DROP PROCEDURE IF EXISTS `addAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addAi`(
  IN `param_aiid` VARCHAR(50),
  IN `param_ai_name` VARCHAR(50),
  IN `param_ai_description` VARCHAR(250),
  IN `param_dev_id` VARCHAR(50),
  IN `param_is_private` TINYINT(1),
  IN `param_client_token` VARCHAR(250),
  IN `param_ui_ai_language` VARCHAR(10),
  IN `param_ui_ai_timezone` VARCHAR(50),
  IN `param_ui_ai_confidence` DOUBLE,
  IN `param_ui_ai_personality` BOOLEAN,
  IN `param_ui_ai_voice` INT(11))
    MODIFIES SQL DATA
BEGIN

  DECLARE var_exists_count INT;
  DECLARE var_named_aiid VARCHAR(50);

  SELECT count(aiid), min(aiid) INTO var_exists_count, var_named_aiid
  FROM ai WHERE `param_dev_id`=`ai`.`dev_id` AND `param_ai_name`=`ai`.`ai_name` AND `ai`.`deleted` = 0;

  IF var_exists_count=0 THEN
    INSERT INTO ai (aiid, ai_name, ai_description, dev_id, is_private,
                    client_token,
                    ui_ai_language, ui_ai_timezone, ui_ai_confidence, ui_ai_personality, ui_ai_voice,
                    default_chat_responses)
    VALUES (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private,
                        param_client_token,
                        param_ui_ai_language,
                        param_ui_ai_timezone, param_ui_ai_confidence, param_ui_ai_personality, param_ui_ai_voice,
            '["Erm...What?"]');
    SET var_named_aiid = `param_aiid`;
  END IF;

  SELECT var_named_aiid AS aiid;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addBotTemplate` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addBotTemplate`(
  IN `param_botId` INT(11),
  IN `param_template` TEXT
)
BEGIN
    INSERT INTO botTemplate (botId, template) VALUES (param_botId, param_template);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addEntityValue` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `addEntityValue`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_value VARCHAR(250))
BEGIN
    INSERT INTO `entity_value` (`entity_id`,`value`)
      SELECT `id`, `in_value` FROM `entity`
      WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addIntentResponse` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addIntentResponse`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_response VARCHAR(250))
BEGIN
    INSERT INTO `intent_response` (`intent_id`, `response`)
      SELECT `id`, `in_response` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                               (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addIntentUserSays` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addIntentUserSays`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_says VARCHAR(250))
BEGIN
    INSERT INTO `intent_user_says` (`intent_id`, `says`)
      SELECT `id`, `in_says` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                           (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addIntentVariablePrompt` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addIntentVariablePrompt`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT,
  IN in_prompt VARCHAR(250)
)
BEGIN

    INSERT INTO `intent_variable_prompt` (`intent_variable_id`, `prompt`)
      SELECT `intent_variable`.`id`, `in_prompt`
      FROM `intent_variable`, `intent`
      WHERE `in_intent_variable_id` = `intent_variable`.`id`
            AND `intent_variable`.`intent_id` = `intent`.`id`
            AND `in_aiid` = `intent`.`aiid`
            AND `intent`.`aiid` IN
                (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addUpdateEntity` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `addUpdateEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_new_name VARCHAR(250))
BEGIN
    INSERT INTO `entity` (`dev_id`, `name`) VALUES (`in_dev_id`, `in_name`)
    ON DUPLICATE KEY UPDATE `name`=`in_new_name`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addUpdateIntent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250),
  IN in_new_name varchar(250),
  IN in_topic_in varchar(250),
  IN in_topic_out varchar(250)
)
BEGIN
    INSERT INTO `intent` (`aiid`, `name`, `topic_in`, `topic_out`)
      SELECT `aiid`, `in_name`, `in_topic_in`, `in_topic_out`
      FROM ai
      WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`
    ON DUPLICATE KEY UPDATE `topic_in`=`in_topic_in`, `topic_out`=`in_topic_out`, `name`=`in_new_name`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addUpdateIntentVariable` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntentVariable`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_name VARCHAR(250),
  IN in_entity_name VARCHAR(250),
  IN in_required int(1),
  IN in_n_prompts int,
  IN in_value varchar(250),
  IN in_label varchar(50)
)
BEGIN
    DECLARE update_count INT;

    INSERT INTO `intent_variable` (`intent_id`, `entity_id`, `required`, `n_prompts`, `value`, `label`)
      SELECT `intent`.`id`, `entity`.`id`, `in_required`, `in_n_prompts`, `in_value`, `in_label`
      FROM `intent`, `entity`
      WHERE `intent`.`id` =
            (SELECT `id` FROM `intent` 
				WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
					(SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`))
            AND `entity`.`id` =
                (SELECT `id` FROM `entity` WHERE 
					(`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1)
					AND `in_entity_name`=`name`)
    ON DUPLICATE KEY UPDATE
      `required`=`in_required`, `n_prompts`=`in_n_prompts`, `value`=`in_value`, `label`=`in_label`,
      `entity_id`= (SELECT `id` FROM `entity` WHERE (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1) AND `in_entity_name`=`name`),
      `dummy` = NOT `dummy`,
      `id` = LAST_INSERT_ID(`intent_variable`.`id`);

    SET update_count = row_count();

    SELECT update_count AS `update`, IF (update_count>0, last_insert_id(), -1) AS `affected_id`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addUser` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `addUser`(
  IN `username` VARCHAR(50),
  IN `email` TINYTEXT,
  IN `password` VARCHAR(64),
  IN `password_salt` VARCHAR(250),
  IN `first_name` VARCHAR(30),
  IN `last_name` VARCHAR(30),
  IN `dev_token` VARCHAR(250),
  IN `plan_id` INT,
  IN `dev_id` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    INSERT INTO `users`(`username`, `email`, `password`, `password_salt`, `first_name`, `last_name`, `dev_token`, `plan_id`, `dev_id`)
    VALUES (username, email, password,password_salt, first_name,last_name, dev_token,plan_id, dev_id);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addUserComplete` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `addUserComplete`(IN `param_username` varchar(50), IN `param_email` tinytext, IN `param_password` varchar(64),IN `param_password_salt` varchar(20), IN `param_name` varchar(30),IN `param_created` datetime,IN `param_attempt` varchar(15),IN `param_dev_token` varchar(250),IN `param_plan_id` int(11),IN `param_dev_id` varchar(50))
    MODIFIES SQL DATA
BEGIN
    insert into users (username, email, password,password_salt,name,created,attempt,dev_token,plan_id,dev_id)
    values (param_username, param_email, param_password, param_password_salt, param_name, param_created, param_attempt, param_dev_token, param_plan_id, param_dev_id);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addWebhook` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addWebhook`(
    IN `aiid` VARCHAR(50), 
    IN `intent_name` VARCHAR(250), 
    IN `endpoint` VARCHAR(2048), 
    IN `enabled` INT(1))
    MODIFIES SQL DATA
BEGIN
INSERT INTO `webhooks`(`aiid`, `intent_name`, `endpoint`, `enabled`)
VALUES (aiid, intent_name, endpoint, enabled);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `checkIntegrationUser` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `checkIntegrationUser`(
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_devid` VARCHAR(50))
BEGIN

SELECT COUNT(`dev_id`) AS 'use_count'
FROM `ai_integration` 
INNER JOIN `ai` ON `ai_integration`.`aiid`=`ai`.`aiid`
WHERE `integration`=`in_integration`
AND `integrated_userid` = `in_integrated_userid`
AND `dev_id`!=`in_devid`;
    
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `deleted` = 1
		WHERE `dev_id`=`in_dev_id` AND `aiid`=`in_aiid`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteAiStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    DELETE FROM `ai_status`
		WHERE `ai_status`.`server_type`=`in_server_type`
        AND `ai_status`.`aiid` = `in_aiid`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteAllAIs` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAllAIs`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `deleted` = 1 WHERE `dev_id`=param_devid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteAllMemoryIntents` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `deleteAllMemoryIntents`(IN `param_aiid` VARCHAR(50))
BEGIN
    DELETE FROM memoryIntent WHERE aiid = param_aiid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteEntity` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_entity_id int(11))
BEGIN
    DELETE FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_entity_id`=`id`
	    AND NOT EXISTS (
		 SELECT NULL FROM `intent_variable` 
		 WHERE `intent_variable`.`entity_id`=`in_entity_id`);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteEntityValue` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntityValue`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_value VARCHAR(250))
BEGIN
    DELETE FROM `entity_value` WHERE `in_value`=`value` AND `entity_id`=
                                                            (SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteIntegration` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `deleteIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50))
BEGIN

DELETE `ai_integration` 
FROM `ai_integration` INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `ai`.`dev_id` = `in_devid`
AND `ai_integration`.`aiid`=`in_aiid` 
AND `integration`=`in_integration`;

    
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteIntent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250)
)
BEGIN
    DELETE FROM `intent`
    WHERE `in_aiid`=`aiid`
          AND `in_name`=`name`
          AND `aiid` IN
              (SELECT `aiid` FROM ai
              WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentResponse` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntentResponse`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_response VARCHAR(250))
BEGIN
    DELETE FROM `intent_response`
    WHERE `in_response`=`response` AND `intent_id`=
                                       (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                                                  (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentUserSays` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntentUserSays`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_says VARCHAR(250))
BEGIN
    DELETE FROM `intent_user_says`
    WHERE `in_says`=`says` AND `intent_id`=
                               (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                                          (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentVariable` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntentVariable`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT
)
BEGIN

    DELETE FROM `intent_variable`
    WHERE `in_intent_variable_id`=`intent_variable`.`id`
          AND `intent_id` IN
              (SELECT `id` FROM `intent` WHERE `in_aiid`=`aiid` AND `in_aiid` IN
                                                                    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteIntentVariablePrompt` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntentVariablePrompt`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT,
  IN in_prompt VARCHAR(250)
)
BEGIN

    DELETE FROM `intent_variable_prompt` WHERE
      `intent_variable_prompt`.`intent_variable_id`=`in_intent_variable_id` AND
      `intent_variable_prompt`.`prompt`=`in_prompt` AND
      `in_intent_variable_id` IN
      (SELECT `intent_variable`.`id`
       FROM `intent_variable`, `intent`
       WHERE `intent_variable`.`intent_id` = `intent`.`id`
             AND `in_aiid` = `intent`.`aiid`
             AND `in_aiid` IN
                 (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteMemoryIntent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `deleteMemoryIntent`(
  IN `param_name` VARCHAR(50),
  IN `param_aiid` VARCHAR(50),
  IN `param_chatId` VARCHAR(50))
BEGIN
    DELETE FROM memoryIntent WHERE aiid = param_aiid AND name = param_name AND chatId = param_chatId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deletePasswordResetToken` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `deletePasswordResetToken`(IN `param_token` VARCHAR(255))
    MODIFIES SQL DATA
BEGIN
    DELETE FROM resetTokens WHERE `token`=param_token;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteUser` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userDeleter`@`127.0.0.1` PROCEDURE `deleteUser`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
    delete from users where dev_id=param_devid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteWebhook` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `deleteWebhook`(
	IN `param_aiid` VARCHAR(50), IN `param_intent_name` VARCHAR(250))
    READS SQL DATA
BEGIN
  DELETE FROM `webhooks`
  WHERE `webhooks`.`intent_name`=`param_intent_name`
  AND `webhooks`.`aiid`=`param_aiid`;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `existsAiTrainingFile` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `existsAiTrainingFile`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN

    SELECT count(aiid) as `ai_trainingfile`
    FROM ai_training WHERE `param_aiid`=`ai_training`.`aiid`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `existsInviteCode` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `existsInviteCode`(IN `param_inviteCode` VARCHAR(50))
    READS SQL DATA
BEGIN

    SELECT count(*)
    FROM invite_codes WHERE `invite_codes`.`code`=`param_inviteCode` AND `invite_codes`.`remaining_uses` > 0;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN

    SELECT
      `ai`.`id`,
      `ai`.`aiid`,
      `ai_name`,
      `ai_description`,
      `created_on`,
      `ai`.`dev_id`,
      `is_private`,
      `client_token`,
      `hmac_secret`,
      `ui_ai_language`,
      `ui_ai_timezone`,
      `ui_ai_confidence`,
      `ui_ai_personality`,
      `ui_ai_voice`,
      `passthrough_url`,
      `default_chat_responses`,
      `api_keys_desc`,
      `botStore`.`publishing_state` as `publishing_state`,
      (SELECT COUNT(`ai_training`.`aiid`)
       FROM `ai_training`
       WHERE `ai_training`.`aiid`=`in_aiid`)
        AS `has_training_file`
    FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;

  END;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiIntegratedResource` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiIntegratedResource`(
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250))
BEGIN

SELECT `ai_integration`.`aiid`, `ai`.`dev_id`, `integrated_userid`, `data`, `status`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `integration`=`in_integration`
AND `integrated_resource`=`in_integrated_resource`
AND `active`>0;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiIntegration` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiIntegrationForUpdate`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50))
BEGIN

SELECT `integrated_resource`, `integrated_userid`, `data`, `status`, `active`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `ai`.`dev_id` = `in_devid`
AND `ai_integration`.`aiid`=`in_aiid` 
AND `integration`=`in_integration`
FOR UPDATE;
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAIQueueStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIQueueStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
		`ai_status`.`server_type`,
		`ai_status`.`aiid`,
        `ai`.`dev_id`,
		`ai_status`.`training_status`,
		`ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_time`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`,
        `ai`.`deleted`
        FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid` = `in_aiid`
    AND `ai_status`.`server_type` = `in_server_type`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAIs` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIs`(
  IN `in_dev_id` VARCHAR(50))
    READS SQL DATA
BEGIN

  SELECT
    `ai`.`id`,
    `ai`.`aiid`,
    `ai`.`ai_name`,
    `ai`.`ai_description`,
    `ai`.`created_on`,
    `ai`.`dev_id`,
    `ai`.`is_private`,
    `ai`.`client_token`,
    `ai`.`hmac_secret`,
    `ai`.`ui_ai_language`,
    `ai`.`ui_ai_timezone`,
    `ai`.`ui_ai_confidence`,
    `ai`.`ui_ai_personality`,
    `ai`.`ui_ai_voice`,
    `ai`.`passthrough_url`,
    `ai`.`default_chat_responses`,
    `ai`.`api_keys_desc`,
    (SELECT COUNT(`ai_training`.`aiid`)
     FROM `ai_training`
     WHERE `ai_training`.`aiid`=`ai`.`aiid`)
      AS `has_training_file`,
    `botStore`.`publishing_state`
  FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`deleted`=0;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAIsForEntity` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getAIsForEntity`(IN `param_devid` VARCHAR(50), IN `param_entity_name` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT i.aiid FROM intent i
      INNER JOIN intent_variable iv ON i.id=iv.intent_id
      INNER JOIN entity e ON iv.entity_id = e.id
    WHERE e.name = param_entity_name
          AND e.dev_id = param_devid;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiSimple` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiSimple`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
      `id`,
      `dev_id`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAIsServerStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIsServerStatus`(
  IN `in_server_type` VARCHAR(10))
    READS SQL DATA
BEGIN
    SELECT
		`ai_status`.`aiid`,
        `ai`.`dev_id`,
		`ai_status`.`training_status`,
		`ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_time`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`deleted` = 0
    AND `ai_status`.`server_type` = `in_server_type`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAIsStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIsStatus`(
  IN `in_dev_id` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
		`ai_status`.`server_type`,
		`ai_status`.`aiid`,
		`ai_status`.`training_status`,
		`ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`dev_id` = `in_dev_id`
    AND `ai`.`deleted` = 0;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiStatus`(
  IN `param_aiid` VARCHAR(50),
  IN `param_devid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
		`ai_status`.`server_type`,
        `ai_status`.`aiid`,
		`ai_status`.`training_status`,
		`ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid`=`param_aiid`
    AND `ai`.`dev_id`=`param_devid`
    AND `ai`.`deleted` = 0;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiStatusAll` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiStatusAll`()
    READS SQL DATA
BEGIN
    SELECT
		`ai_status`.`server_type`,
		`ai_status`.`aiid`,
		`ai_status`.`training_status`,
		`ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`deleted` = 0;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiStatusForUpdate` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiStatusForUpdate`(
  IN `param_aiid` VARCHAR(50),
  IN `param_devid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
		`ai_status`.`server_type`,
        `ai_status`.`aiid`,
		`ai_status`.`training_status`,
		`ai_status`.`training_progress`,
        `ai_status`.`training_error`,
        `ai_status`.`queue_action`,
        `ai_status`.`server_endpoint`,
        `ai_status`.`update_time`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid`=param_aiid
    AND `ai`.`dev_id`=param_devid
    AND `ai`.`deleted` = 0
    FOR UPDATE;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiTrainingFile` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiTrainingFile`(IN `param_aiid` varchar(50))
    READS SQL DATA
BEGIN

    SELECT ai_trainingfile FROM ai_training WHERE aiid=param_aiid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiVoice` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiVoice`(IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT `ai_voice`
    FROM `ai`
    WHERE `aiid`=`param_aiid`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotDetails` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getBotDetails`(IN `param_botId` INT(11))
    NO SQL
BEGIN
    SELECT * FROM botStore WHERE id = param_botId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotIcon` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getBotIcon`(IN `param_botId` INT(11))
    NO SQL
BEGIN
    SELECT botIcon FROM botStore WHERE id = param_botId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotsLinkedToAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getBotsLinkedToAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50))
NO SQL
  BEGIN
    SELECT bs.* FROM botStore bs INNER JOIN bot_ai bai ON bai.botId = bs.id WHERE bai.aiid = param_aiid AND bai.dev_id = param_devId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAisLinkedToAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAisLinkedToAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50))
BEGIN
	SELECT bai.aiid as 'ai', bs.aiid as 'linked_ai', bs.dev_id as 'linked_ai_devId', ai.ui_ai_confidence as 'minP'
	FROM bot_ai bai INNER JOIN botStore bs ON bs.id = bai.botId INNER JOIN ai ai ON ai.aiid = bs.aiid
	WHERE bai.aiid=param_aiid AND bai.dev_id=param_devId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotstoreItem` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotstoreItem`(
	IN `param_botId` INT)
BEGIN

SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website', ai.api_keys_desc as 'api_keys_desc'
FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id INNER JOIN ai ON ai.aiid = bs.aiid WHERE bs.publishing_state=2 AND bs.id = param_botId;

END;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotstoreList` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotstoreList`(
	IN `param_filters` VARCHAR(255),
    IN `param_order` VARCHAR(50),
    IN `param_pageStart` INT,
    IN `param_pageSize` INT)
BEGIN

SET @select_query = "SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website' ";
SET @from_query = "FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id WHERE publishing_state=2 ";
IF LENGTH(param_filters) = 0 THEN
	SET @where_other = "";
ELSE
	SET @where_other = concat(" AND ", param_filters);
END IF;
IF LENGTH(param_order) = 0 THEN
	SET @orderBy = "";
ELSE
	SET @orderBy = concat(" ORDER BY ", param_order);
END IF;
SET @limitTo = concat(concat(concat(" LIMIT ", param_pageStart), ", "), param_pageSize);
SET @query = concat(concat(concat(concat(@select_query, @from_query), @where_other), @orderBy), @limitTo);
PREPARE stmt3 FROM @query;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;


SET @select_query = "SELECT COUNT(*) as 'total'";
SET @query = concat(concat(@select_query, @from_query), @where_other);
PREPARE stmt3 FROM @query;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotstoreListPerCategory` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotstoreListPerCategory`(
	IN `param_max` INT(11))
BEGIN
	SET @select_query =
    "SELECT * FROM(
		SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website',
			(@num:=if(@group = bs.category, @num +1, if(@group := bs.category, 1, 1))) row_number
		FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id
		CROSS JOIN (select @num:=0, @group:=null) c
		WHERE publishing_state=2
        ORDER BY bs.category
	) as x
    WHERE x.row_number <= ";

    SET @limitTo = param_max;

    SET @query = concat(@select_query, @limitTo);

    PREPARE stmt3 FROM @query;
	EXECUTE stmt3;
	DEALLOCATE PREPARE stmt3;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotTemplate` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotTemplate`(
  IN `param_botId` INT(11)
)
BEGIN
    SELECT `template` FROM botTemplate WHERE `botId` = param_botId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getChatState` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `getChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50))
BEGIN
    SELECT * FROM chatState WHERE dev_id = param_devId AND chat_id = param_chatId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDeveloperInfo` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getDeveloperInfo`(IN `param_devid` VARCHAR(50))
BEGIN
    SELECT * FROM developerInfo WHERE dev_id = param_devid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDevPlan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getDevPlan`(IN `in_dev_id` VARCHAR(50))
BEGIN
    SELECT d.*
    FROM devplan d INNER JOIN users u ON u.plan_id = d.plan_id
    WHERE u.dev_id=in_dev_id;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDevToken` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getDevToken`(IN `uid` INT)
    NO SQL
BEGIN
    SELECT `dev_token` FROM `users` WHERE `id` = uid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDevTokenFromDevID` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getDevTokenFromDevID`(IN `devid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT `dev_token` FROM `users` WHERE `dev_id` = devid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDomains` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`domainsReader`@`127.0.0.1` PROCEDURE `getDomains`()
    READS SQL DATA
BEGIN
    SELECT * FROM `domains`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDomainsAndUserActiveDomains` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`domainsReader`@`127.0.0.1` PROCEDURE `getDomainsAndUserActiveDomains`(IN `param_dev_id` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT * FROM `domains` AS d LEFT OUTER JOIN ( SELECT * FROM `userAIDomains` WHERE `dev_id` = param_dev_id AND  `aiid`= param_aiid ) AS u ON u.dom_id = d.dom_id;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getEntities` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntities`(
  IN in_dev_id VARCHAR(50))
BEGIN
    SELECT `name`, `isSystem` FROM `entity` WHERE 
    `entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1;    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getEntityDetails` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityDetails`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT * FROM `entity`
    WHERE `entity`.`name`=`in_name`
          AND (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getEntityIdForDev` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityIdForDev`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
	SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getEntityValues` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityValues`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `entity_value`.`value` FROM `entity`,`entity_value`
    WHERE `entity`.`dev_id`=`in_dev_id`
          AND `entity`.`name`=`in_name`
          AND `entity`.`id`=`entity_value`.`entity_id`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntegratedResource` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getIntegratedResource`(
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250))
BEGIN

SELECT `ai_integration`.`aiid` AS `aiid`, `ai`.`dev_id` AS `devid`,
		`integrated_userid`, `data`, `status`, `active`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `integration`=`in_integration`
AND `integrated_resource`=`in_integrated_resource`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntegrations` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`integrReader`@`127.0.0.1` PROCEDURE `getIntegrations`()
    READS SQL DATA
BEGIN
    SELECT * FROM `integrations`;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntent`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id`, `name`, `topic_in`, `topic_out`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
		AND `intent`.`aiid` = `in_aiid`;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentIDs` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentIDs`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50)
)
BEGIN

    SELECT `intent`.`id` from `intent`, `ai`
    WHERE `ai`.`dev_id` = `in_dev_id`
          AND `ai`.`aiid` = `in_aiid`
          AND `intent`.`aiid` = `in_aiid`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentResponses` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentResponses`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `response`
    FROM `intent_response` WHERE `intent_id` IN
         (SELECT `id` FROM `intent`
          WHERE `in_name`=`name` AND `in_aiid`=`aiid`);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntents` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntents`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT `id`, `name`
    FROM `intent` WHERE `intent`.`id` IN
                        (SELECT `intent`.`id` from `intent`, `ai`
                        WHERE `ai`.`dev_id` = `in_dev_id`
                              AND `ai`.`aiid` = `in_aiid`
                              AND `intent`.`aiid` = `in_aiid`);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentUserSays` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentUserSays`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `says`
    FROM `intent_user_says` WHERE `intent_id` IN
         (SELECT `id` FROM `intent`
          WHERE `in_name`=`name` AND `in_aiid`=`aiid`);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentVariableIDs` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentVariableIDs`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_intent_id INT
)
BEGIN

    SELECT `intent_variable`.`id` FROM `intent_variable`, `intent`, `ai`
    WHERE `ai`.`dev_id` = `in_dev_id`
          AND `ai`.`aiid` = `in_aiid`
          AND `intent`.`aiid` = `in_aiid`
          AND `intent_variable`.`intent_id` = `in_intent_id`
          AND `intent_variable`.`intent_id` = `intent`.`id`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentVariablePrompts` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentVariablePrompts`(
  IN in_aiid VARCHAR(50),
  IN in_intent_variable_id INT
)
BEGIN

    SELECT `prompt` FROM `intent_variable_prompt` WHERE
      `intent_variable_prompt`.`intent_variable_id`=`in_intent_variable_id` AND
      `in_intent_variable_id` IN
      (SELECT `intent_variable`.`id`
       FROM `intent_variable`, `intent`
       WHERE `intent_variable`.`intent_id` = `intent`.`id`
             AND `in_aiid` = `intent`.`aiid`);

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getIntentVariables` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntentVariables`(
  IN in_aiid VARCHAR(50),
  IN in_intent_name VARCHAR(250)
)
BEGIN

    SELECT
      `intent_variable`.`id` AS `id`,
      `entity`.`name` AS `entity_name`,
      `intent_variable`.`required` AS `required`,
      `intent_variable`.`n_prompts` AS `n_prompts`,
      `intent_variable`.`value` AS `value`,
      `entity`.`dev_id` AS `dev_id`,
      `entity`.`isPersistent` as `isPersistent`,
      `intent_variable`.`label` as `label`
    FROM `intent_variable`, `entity`
    WHERE `intent_variable`.`intent_id` =
          (SELECT `id` FROM `intent`
           WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid`)
          AND `entity`.`id` =
              (SELECT `id` FROM `entity`
               WHERE `id` = `intent_variable`.`entity_id`);

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getInterruptedTrainingList` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getInterruptedTrainingList`(
  IN `in_server_type` VARCHAR(10),
  IN `in_training_status` VARCHAR(50),
  IN `in_cutoff_seconds` INT)
BEGIN

DECLARE v_cutoff DATETIME;
SET v_cutoff = DATE_SUB(NOW(), INTERVAL `in_cutoff_seconds` SECOND);

SELECT `ai_status`.*
FROM `ai_status`
WHERE `server_type` = `in_server_type`
AND `ai_status`.`queue_time` IS NULL
AND `ai_status`.`training_status` = `in_training_status`
AND `update_time`<=v_cutoff
ORDER BY `update_time` ASC
FOR UPDATE;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getMemoryIntent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getMemoryIntent`(IN `param_name` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_chatId` VARCHAR(50))
BEGIN
    SELECT * FROM memoryIntent WHERE aiid = param_aiid AND chatId = param_chatId AND `name` = param_name;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getMemoryIntentsForChat` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getMemoryIntentsForChat`(IN `param_aiid` VARCHAR(50), IN `param_chatId` VARCHAR(50))
BEGIN
    SELECT * FROM memoryIntent WHERE aiid = param_aiid AND chatId = param_chatId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getPublishedBotForAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getPublishedBotForAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.* FROM botStore bs
    WHERE bs.dev_id = param_devId AND bs.aiid = param_aiid;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getPublishedBots` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getPublishedBots`(
  IN `param_publishing_type` TINYINT(1)
)
    NO SQL
BEGIN
    SELECT * FROM botStore WHERE publishing_state = 2 AND publishing_type = param_publishing_type;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getPurchasedBots` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getPurchasedBots`(IN `param_devId` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.* FROM botPurchase bp INNER JOIN botStore bs ON bs.id = bp.botId WHERE bp.dev_id = param_devId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getUser` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getUser`(IN `uname` VARCHAR(50), IN `checkEmail` BOOLEAN)
    READS SQL DATA
BEGIN
    IF checkEmail THEN
      SELECT `email`, `id`, `password`, `password_salt`, `attempt`
      FROM users
      WHERE `valid`=1 AND (`username`=uname OR `email`=uname) ORDER BY `id` LIMIT 1;
    ELSE
      SELECT `email`, `id`, `password`, `attempt`
      FROM users
      WHERE `valid`=1 AND `username`=uname ORDER BY `id` LIMIT 1;
    END IF;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getUserById` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getUserById`(IN `idValue` INT(11), IN `columnValues` TINYTEXT)
    READS SQL DATA
BEGIN
    SET @s=CONCAT('SELECT ',columnValues,' FROM users WHERE `id`=', idValue, ' ORDER BY `id` LIMIT 1');
    PREPARE stmt1 FROM @s;
    EXECUTE stmt1;
    DEALLOCATE PREPARE stmt1;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getUserDetails` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getUserDetails`(IN `param_username` VARCHAR(50))
BEGIN
    SELECT `dev_id`, `email`, `first_name`, `created`, `attempt`, `valid`, `internal`, `password`, `password_salt`, `username`, `dev_id`, `id`, `dev_token`
    FROM `users`
    WHERE `username`=param_username;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getUserId` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getUserId`(IN `nameOfUser` VARCHAR(50) CHARSET latin1, IN `checkEmail` BOOLEAN)
    READS SQL DATA
BEGIN
    IF checkEmail THEN
      SELECT id
      FROM users
      WHERE username=nameOfUser OR email=nameOfUser;
    ELSE
      SELECT id
      FROM users
      WHERE username=nameOfUser;
    END IF;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getUserIdForResetToken` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getUserIdForResetToken`(IN `param_token` VARCHAR(255))
BEGIN
    SELECT `uid`
    FROM `resetTokens`
    WHERE `token`=param_token;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getUserIdFromUsername` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `getUserIdFromUsername`(IN `param_username` VARCHAR(255))
BEGIN
    SELECT `dev_id`
    FROM `users`
    WHERE `username`=param_username;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getWebhook` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getWebhook`(
	IN `param_aiid` VARCHAR(50), IN `param_intent_name` VARCHAR(250))
    READS SQL DATA
BEGIN
  SELECT
	`aiid`,
    `intent_name`,
    `endpoint`,
    `enabled`
  FROM `webhooks`
  WHERE `webhooks`.`intent_name`=`param_intent_name` AND `webhooks`.`aiid`=`param_aiid`;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `hasBotBeenPurchased` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `hasBotBeenPurchased`(IN `param_botId` INT(11))
BEGIN
    SELECT EXISTS (SELECT botId FROM botPurchase WHERE botId = param_botId);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `insertResetToken` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `insertResetToken`(IN `param_token` VARCHAR(40), IN `param_userId` INT)
    NO SQL
BEGIN
    DELETE FROM `resetTokens` WHERE `uid` = param_userId;
    INSERT INTO `resetTokens`(`token`, `uid`) VALUES (param_token, param_userId);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `insertUserActiveDomain` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`domainsWriter`@`127.0.0.1` PROCEDURE `insertUserActiveDomain`(IN `param_dev_id` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_dom_id` VARCHAR(50), IN `param_active` BOOLEAN)
    MODIFIES SQL DATA
BEGIN
    IF EXISTS (
        SELECT 1 = 1
        FROM userAIDomains
        WHERE dom_id = param_dom_id
              AND aiid = param_aiid
              AND dev_id = param_dev_id
    )
    THEN
      UPDATE userAIDomains
      SET active = param_active
      WHERE dom_id = param_dom_id
            AND aiid = param_aiid
            AND dev_id = param_dev_id;
    ELSEIF ( param_active=1) THEN
      INSERT INTO userAIDomains (
        dev_id,
        aiid,
        dom_id,
        active,
        created_on
      )
      VALUES (
        param_dev_id,
        param_aiid,
        param_dom_id,
        param_active,
        NOW()
      );
    END IF;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `isPasswordResetTokenValid` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `isPasswordResetTokenValid`(IN `param_token` VARCHAR(255))
    MODIFIES SQL DATA
BEGIN
    SELECT `uid` FROM resetTokens
    WHERE `token`=param_token;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `linkBotToAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `linkBotToAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_botId` INT(11))
    NO SQL
BEGIN
    INSERT INTO bot_ai (botId, dev_id, aiid) VALUES(param_botId, param_devId, param_aiid);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `publishBot` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreWriter`@`127.0.0.1` PROCEDURE `publishBot`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50),
  IN `param_name` VARCHAR(50),
  IN `param_description` VARCHAR(1024),
  IN `param_longDescription` TEXT,
  IN `param_alertMessage` VARCHAR(150),
  IN `param_badge` VARCHAR(20),
  IN `param_price` DECIMAL,
  IN `param_sample` TEXT,
  IN `param_lastUpdate` DATETIME,
  IN `param_category` VARCHAR(50),
  IN `param_licenseType` VARCHAR(50),
  IN `param_privacyPolicy` TEXT,
  IN `param_classification` VARCHAR(50),
  IN `param_version` VARCHAR(25),
  IN `param_videoLink` VARCHAR(1800),
  IN `param_publishingState` TINYINT(1),
  IN `param_publishingType` TINYINT(1)
)
    NO SQL
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
      ROLLBACK;
      SELECT -1;
    END;

    INSERT INTO botStore
    (dev_id, aiid, name, description, long_description, alert_message, badge, price, sample, last_update, category,
     privacy_policy, classification, version, video_link, license_type, publishing_state, publishing_type)
    VALUES (param_devId, param_aiid, param_name, param_description, param_longDescription, param_alertMessage,
                         param_badge, param_price, param_sample, param_lastUpdate, param_category, param_privacyPolicy, param_classification,
            param_version, param_videoLink, param_licenseType, param_publishingState, param_publishingType);

    SELECT LAST_INSERT_ID();
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `purchaseBot` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `purchaseBot`(IN `param_devId` VARCHAR(50), IN `param_botId` INT(11))
    NO SQL
BEGIN
    DECLARE thePrice DECIMAL;
    SELECT price INTO thePrice FROM botStore WHERE id = param_botId;
    INSERT INTO botPurchase (botId, dev_id, price) VALUES (param_botId, param_devId, thePrice);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `queueCountSlots` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueCountSlots`(
  IN `in_server_type` VARCHAR(10),
  IN `in_training_status` VARCHAR(50),
  IN `in_cutoff_seconds` INT)
BEGIN

DECLARE v_cutoff DATETIME;
SET v_cutoff = DATE_SUB(NOW(), INTERVAL `in_cutoff_seconds` SECOND);

SELECT `ai_status`.`server_endpoint`,
 sum(case when `ai_status`.`update_time` > v_cutoff then 1 else 0 end) training,
 sum(case when `ai_status`.`update_time` <= v_cutoff then 1 else 0 end) lapsed
FROM `ai_status`
WHERE `server_type` = `in_server_type`
AND `ai_status`.`queue_time` IS NULL
AND `ai_status`.`training_status` = `in_training_status`
GROUP BY `ai_status`.`server_endpoint`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `queueRecover` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueRecover`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_queue_action` VARCHAR(50),
  IN `in_training_status` VARCHAR(45)
  )
BEGIN

UPDATE `ai_status` SET
	`training_status` = `in_training_status`,
    `queue_action`=`in_queue_action`,
    `update_time`=now(),
    `queue_time`=now()
WHERE `server_type` = `in_server_type`
	AND `aiid` = `in_aiid`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `queueTakeNext` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueTakeNext`(
  IN `in_server_type` VARCHAR(10))
BEGIN

DECLARE v_aiid VARCHAR(50);
SELECT `ai_status`.`aiid` INTO v_aiid
	FROM `ai_status`
	WHERE `server_type` = `in_server_type`
	AND `queue_time`<now()
	ORDER BY `queue_time` ASC
	LIMIT 1 FOR UPDATE;

IF NOT v_aiid IS NULL THEN
    UPDATE `ai_status` SET `queue_time`=NULL
		WHERE `aiid` = v_aiid
        AND `in_server_type`=`server_type`;
END IF;
SELECT * FROM `ai_status`
	WHERE v_aiid IS NOT NULL
	AND `aiid`=v_aiid
	AND `in_server_type`=`server_type`;


  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `queueUpdate` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueUpdate`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_set_queued` TINYINT,
  IN `in_queue_offset` INT,
  IN `in_queue_action` VARCHAR(50)
  )
BEGIN

DECLARE v_queue_time DATETIME;
IF (`in_set_queued` = 0) THEN
	SET v_queue_time = NULL;
ELSE
	SET v_queue_time = now() + INTERVAL `in_queue_offset` SECOND;
END IF;

UPDATE `ai_status` SET
	`queue_time`=v_queue_time,
    `queue_action`=`in_queue_action`,
    `update_time`=now()
WHERE `server_type` = `in_server_type`
	AND `aiid` = `in_aiid`;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `rateLimitCheck` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`rateLimiter`@`127.0.0.1` PROCEDURE `rateLimitCheck`(
  IN `in_dev_id` VARCHAR(50) CHARSET utf8,
  IN `in_rate_key` VARCHAR(50) CHARSET utf8,
  IN `token_ceiling` FLOAT,
  IN `token_increment_delay_seconds` FLOAT)
    MODIFIES SQL DATA
BEGIN

    DECLARE time_now BIGINT;
    DECLARE var_uuid VARCHAR(50);
    DECLARE user_valid tinyint;

    SET var_uuid = uuid();
    SET time_now = CONV(CONCAT(SUBSTR(var_uuid, 16, 3),SUBSTR(var_uuid, 10, 4),SUBSTR(var_uuid, 1, 8)), 16, 10) / 10000 - (141427 * 24 * 60 * 60);

    SELECT count(*) INTO user_valid
    FROM users
    WHERE users.dev_id = in_dev_id
          AND users.valid > 0;

    IF NOT user_valid THEN
      SELECT 1 AS rate_limit, 0.0 AS tokens, 0 AS valid;
    ELSE
      INSERT INTO api_rate_limit (dev_id, rate_key, tokens, token_update_time)
      VALUES (in_dev_id, in_rate_key, token_ceiling, time_now)
      ON DUPLICATE KEY UPDATE
        tokens = LEAST(tokens + (time_now - token_update_time)/(1000.0 * token_increment_delay_seconds), token_ceiling),
        token_update_time = time_now;

      UPDATE api_rate_limit SET
        tokens = tokens-1.0,
        expires = now() + INTERVAL (token_ceiling * token_increment_delay_seconds) SECOND
      WHERE
        (dev_id = in_dev_id AND rate_key = in_rate_key)
        AND tokens >= 1.0;

      SELECT IF(ROW_COUNT()>0, 0, 1) AS rate_limit, tokens, 1 AS valid
      FROM api_rate_limit
      WHERE dev_id = in_dev_id AND rate_key = in_rate_key;
    END IF;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `redeemInviteCode` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `redeemInviteCode`(IN `param_inviteCode` VARCHAR(50), IN `param_username` VARCHAR(50))
    READS SQL DATA
BEGIN

    UPDATE `invite_codes`
    SET `remaining_uses` = `remaining_uses` - 1
    WHERE `code` = `param_inviteCode`
          AND `remaining_uses` > 0;

    INSERT INTO `invite_code_uses`(`invite_code`, `username`)
    VALUES (param_inviteCode, param_userName);

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `saveBotIcon` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreWriter`@`127.0.0.1` PROCEDURE `saveBotIcon`(IN `param_devId` VARCHAR(50), IN `param_botId` INT(11), IN `param_filename` VARCHAR(255))
    NO SQL
BEGIN
    UPDATE botStore
    SET  botIcon = param_filename
    WHERE dev_id = param_devId AND id = param_botId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `setChatState` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_timestamp` TIMESTAMP,
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE)
BEGIN
    INSERT INTO chatState (dev_id, chat_id, timestamp, topic, history, locked_aiid, entity_values, confidence_threshold)
    VALUES(param_devId, param_chatId, param_timestamp, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold)
    ON DUPLICATE KEY UPDATE timestamp = param_timestamp, topic = param_topic, history = param_history,
      locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `setDeveloperInfo` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `setDeveloperInfo`(
  IN `param_devid` VARCHAR(50),
  IN `param_name` varchar(100),
  IN `param_company` varchar(100),
  IN `param_email` varchar(100),
  IN `param_address` varchar(200),
  IN `param_postCode` varchar(100),
  IN `param_city` varchar(100),
  IN `param_country` varchar(100),
  IN `param_website` varchar(1024))
BEGIN
    INSERT INTO developerInfo
    (`dev_id`, `name`,`company`,`email`,`address`,`post_code`,`city`,`country`,`website`)
    VALUES (param_devid,param_name,param_company,param_email,param_address,param_postCode,param_city,param_country,param_website);
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `unlinkBotFromAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `unlinkBotFromAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_botId` INT(11))
    NO SQL
BEGIN
    DELETE FROM bot_ai
    WHERE botId = param_botId AND aiid = param_aiid AND dev_id = param_devId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAi` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAi`(
  IN `param_aiid` VARCHAR(50),
  IN `param_ai_description` VARCHAR(250),
  IN `param_dev_id` VARCHAR(50),
  IN `param_is_private` TINYINT(1),
  IN `param_ui_ai_language` VARCHAR(10),
  IN `param_ui_ai_timezone` VARCHAR(50),
  IN `param_ui_ai_confidence` DOUBLE,
  IN `param_ui_ai_personality` TINYINT(4),
  IN `param_ui_ai_voice` VARCHAR(50),
  IN `param_default_chat_responses` TEXT)
    MODIFIES SQL DATA
BEGIN

    update ai
    set
      ai_description = param_ai_description,
      is_private = param_is_private,
      ui_ai_language = param_ui_ai_language,
      ui_ai_timezone = param_ui_ai_timezone,
      ui_ai_confidence = param_ui_ai_confidence,
      ui_ai_personality = param_ui_ai_personality,
      ui_ai_voice = param_ui_ai_voice,
      default_chat_responses = param_default_chat_responses
    where aiid = param_aiid AND dev_id = param_dev_id;

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAiIntegration` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_data` JSON,
  IN `in_status` VARCHAR(1024),
  IN `in_active` TINYINT,
  IN `in_deactivate_message` VARCHAR(250))
BEGIN

IF NOT (NULLIF(`in_integrated_resource`, '') IS NULL) THEN
	UPDATE `ai_integration` SET 
		`integrated_resource`='',
        `status`=`in_deactivate_message`,
		`active`=0
        WHERE `ai_integration`.`integration`=`in_integration`
        AND `ai_integration`.`aiid`!=`in_aiid`
        AND `in_integrated_resource` = `ai_integration`.`integrated_resource`;
END IF;

INSERT INTO `ai_integration`
  (`aiid`, `integration`, 
  `integrated_resource`, `integrated_userid`,
  `data`,`status`, `active`,
  `update_time`)
(SELECT
  `in_aiid`, `in_integration`,
  `in_integrated_resource`,
  `in_integrated_userid`,
  `in_data`, `in_status`, `in_active`,
  now() 
FROM `ai`
WHERE `ai`.`aiid` = `in_aiid` AND `ai`.`dev_id` = `in_devid`)
ON DUPLICATE KEY UPDATE
	`integrated_resource` = `in_integrated_resource`,
    `integrated_userid` = `in_integrated_userid`,
	`data`=`in_data`,
    `status`=`in_status`,
    `active`=`in_active`,
    `update_time`=now();
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAiStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_training_status` VARCHAR(45),
  IN `in_server_endpoint` VARCHAR(256),
  IN `in_training_progress` FLOAT,
  IN `in_training_error` FLOAT)
BEGIN

INSERT INTO `ai_status`
( `server_type`,
  `aiid`,
  `training_status`,
  `training_progress`,
  `training_error`,
  `server_endpoint`)
VALUES
( `in_server_type`,
  `in_aiid`,
  `in_training_status`,
  `in_training_progress`,
  `in_training_error`,
  `in_server_endpoint`)
ON DUPLICATE KEY UPDATE
	`training_status`=`in_training_status`,
    `training_progress`=`in_training_progress`,
    `training_error`=`in_training_error`,
	`server_endpoint`=`in_server_endpoint`,
    `update_time`=now();
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAiTrainingFile` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAiTrainingFile`(IN `param_aiid` varchar(50), IN `param_ai_trainingfile` LONGTEXT)
    MODIFIES SQL DATA
BEGIN

    INSERT INTO `ai_training` (ai_training.aiid, ai_training.ai_trainingfile, ai_training.updated)
    VALUES (`param_aiid`, `param_ai_trainingfile`, now())
    ON DUPLICATE KEY UPDATE
      ai_training.ai_trainingfile = `param_ai_trainingfile`,
      ai_training.updated = now();

  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateBotPublishingState` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateBotPublishingState`(IN `param_botId` INT(11), IN `param_publishingState` TINYINT(1))
BEGIN
    UPDATE botStore SET publishing_state = param_publishingState WHERE id = param_botId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateControllerState` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateControllerState`(
  IN `in_server_type` VARCHAR(10),
  IN `in_verified_server_count` INT,
  IN `in_training_capacity` INT,
  IN `in_training_slots_available` INT,
  IN `in_chat_capacity` INT)
BEGIN
INSERT INTO `controller_state`
  (`server_type`,
  `verified_server_count`,
  `training_capacity`,
  `training_slots_available`,
  `chat_capacity`,
  `update_time`)
VALUES
  (`in_server_type`,
  `in_verified_server_count`,
  `in_training_capacity`,
  `in_training_slots_available`,
  `in_chat_capacity`,
  now())
ON DUPLICATE KEY UPDATE
  `verified_server_count`=`in_verified_server_count`,
  `training_capacity`=`in_training_capacity`,
  `training_slots_available`=`in_training_slots_available`,
  `chat_capacity`=`in_chat_capacity`,
  `update_time`=now();
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateIntegrationStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateIntegrationStatus`(
  IN `in_aiid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_status` VARCHAR(1024),
  IN `in_set_chat_now` TINYINT)
BEGIN

UPDATE `ai_integration` SET 
 `status` = `in_status`,
 `chat_time` = CASE WHEN `in_set_chat_now`>0 THEN now() ELSE `chat_time` END,
 `update_time` = now()
 WHERE `aiid`=`in_aiid` 
 AND `integration`=`in_integration`;
 
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateMemoryIntent` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `updateMemoryIntent`(IN `param_name` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_chatId` VARCHAR(50),
                                                                            IN `param_variables` MEDIUMTEXT, IN `param_isFulFilled` TINYINT(1))
BEGIN
    INSERT INTO memoryIntent (aiid, chatId, name, variables, lastAccess, isFulfilled)
    VALUES(param_aiid, param_chatId, param_name, param_variables, NOW(), param_isFulFilled)

    ON DUPLICATE KEY UPDATE variables = param_variables, lastAccess = NOW(), isFulfilled = param_isFulFilled;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateUserLoginAttempts` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `updateUserLoginAttempts`(IN `param_devId` VARCHAR(50), IN `param_attempt` VARCHAR(15))
    MODIFIES SQL DATA
BEGIN
    UPDATE `users` SET `attempt` = param_attempt WHERE `dev_id`=param_devId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateUserPassword` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `updateUserPassword`(IN `param_userId` INT(11), IN `param_password` VARCHAR(255), IN `param_passwordSalt` VARCHAR(255))
    MODIFIES SQL DATA
BEGIN
    UPDATE `users`
    SET `password` = param_password,
      `password_salt` = param_passwordSalt
    WHERE `id`=param_userId;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateWebhook` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateWebhook`(
    IN `param_aiid` VARCHAR(50), 
    IN `param_intent_name` VARCHAR(250), 
    IN `param_endpoint` VARCHAR(2048), 
    IN `param_enabled` INT(1))
    MODIFIES SQL DATA
BEGIN
UPDATE webhooks
SET
	endpoint = param_endpoint,
    enabled = param_enabled
WHERE aiid = param_aiid AND intent_name = param_intent_name;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `userExists` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`userTableReader`@`127.0.0.1` PROCEDURE `userExists`(IN `param_username` VARCHAR(155), IN `param_checkEmail` TINYINT(1))
BEGIN
    IF param_checkEmail THEN
      SELECT `email`, `id`, `password`, `password_salt`, `attempt`
      FROM users
      WHERE `valid`=1 AND (`username`=param_username OR `email`=param_username) ORDER BY `id` LIMIT 1;
    ELSE
      SELECT `email`, `id`, `password`, `attempt`
      FROM users
      WHERE `valid`=1 AND `username`=param_username ORDER BY `id` LIMIT 1;
    END IF;
  END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getWebhookSecretForBot` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getWebhookSecretForBot`(IN `param_aiid` VARCHAR(50))
BEGIN
    SELECT `hmac_secret` FROM `ai` WHERE `aiid` = `param_aiid`;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `setWebhookSecretForBot` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setWebhookSecretForBot`(
  IN `in_aiid` VARCHAR(50),
  IN `in_hmac_secret` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `hmac_secret` = `in_hmac_secret`
		WHERE `aiid`=`in_aiid`;
END ;;
DELIMITER ;

DELIMITER ;
DROP procedure IF EXISTS `getAiBotConfig`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiBotConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_bot_id` INT(11))
BEGIN
	SELECT * FROM `bot_ai_config` WHERE `dev_id` = `in_dev_id` AND `aiid` = `in_aiid` AND `botId` = `in_bot_id`;
END;;

DELIMITER ;
DROP procedure IF EXISTS `setAiBotConfig`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setAiBotConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_botId` INT(11),
  IN `in_config` JSON)
    MODIFIES SQL DATA
BEGIN
  INSERT INTO `bot_ai_config` SET
    `dev_id` = `in_dev_id`,
    `aiid` = `in_aiid`,
    `botId` = `in_botId`,
    `config`= `in_config`
  ON DUPLICATE KEY UPDATE `config` = `in_config`;
END;;

DELIMITER ;
DROP procedure IF EXISTS `getIsBotLinkedToAi`;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getIsBotLinkedToAi`(
	IN `param_devId` VARCHAR(50), 
    IN `param_aiid` VARCHAR(50),
    IN `param_botId` INT(11))
    NO SQL
BEGIN
    SELECT bs.* FROM botStore bs INNER JOIN bot_ai bai ON bai.botId = bs.id WHERE bai.aiid = param_aiid AND bai.dev_id = param_devId AND bai.botId = param_botId;
END;;

DELIMITER ;
DROP procedure IF EXISTS `getBotConfigForWebhookCall`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotConfigForWebhookCall`(
	IN `param_devId` VARCHAR(50), 
    IN `param_aiid` VARCHAR(50),
    IN `param_aiidLinkedBot` VARCHAR(50))
    NO SQL
BEGIN
	IF `param_aiid` = `param_aiidLinkedBot` THEN
		SELECT bac.config AS `config` FROM bot_ai_config bac 
		WHERE bac.aiid = param_aiid AND bac.dev_id = param_devId AND bac.botId = 0;
    ELSE
		SELECT bac.config AS `config` FROM bot_ai_config bac 
		INNER JOIN botStore bs ON bac.botId = bs.id 
		WHERE bac.aiid = param_aiid AND bac.dev_id = param_devId AND bs.aiid = param_aiidLinkedBot;
	END IF;
END;;

#
# DB update for validating bot configuration JSON
#
DELIMITER ;
DROP procedure IF EXISTS `setApiKeyDescriptions`;
DROP procedure IF EXISTS `setBotConfigDefinition`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setBotConfigDefinition`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_config_def` JSON)
    MODIFIES SQL DATA
BEGIN
	UPDATE ai
    SET `api_keys_desc` = `in_config_def` 
    WHERE aiid = in_aiid AND dev_id = in_dev_id;
END;;


DELIMITER ;
DROP procedure IF EXISTS `getBotConfigDefinition`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotConfigDefinition`(
	IN `param_devId` VARCHAR(50), 
    IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT api_keys_desc FROM ai WHERE aiid = param_aiid AND dev_id = param_devId;
END;;

/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-06-07 14:15:08
