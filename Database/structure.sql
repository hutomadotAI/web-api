CREATE DATABASE  IF NOT EXISTS `hutoma` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `hutoma`;
-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: hutoma
-- ------------------------------------------------------
-- Server version	5.7.16

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
  `created_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `dev_id` varchar(50) NOT NULL,
  `is_private` tinyint(1) DEFAULT '1',
  `client_token` varchar(250) NOT NULL,
  `backend_status` json DEFAULT NULL,
  `ui_ai_language` varchar(10) DEFAULT 'en-US',
  `ui_ai_timezone` varchar(50) DEFAULT 'UTC',
  `ui_ai_confidence` double DEFAULT NULL,
  `ui_ai_personality` tinyint(4) DEFAULT '0',
  `ui_ai_voice` int(11) DEFAULT '0',
  'deleted' tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `aiid_UNIQUE` (`aiid`)
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
  `is_published` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `dev_id` (`dev_id`),
  KEY `aiid` (`aiid`),
  CONSTRAINT `botStore_ibfk_1` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE,
  CONSTRAINT `botStore_ibfk_2` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_id` (`intent_id`,`entity_id`),
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
-- Table structure for table `memoryIntent`
--

DROP TABLE IF EXISTS `memoryIntent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `memoryIntent` (
  `aiid` varchar(50) NOT NULL,
  `chatId` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `variables` text NOT NULL,
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
  `client_token` varchar(250) NOT NULL,
  `last_name` varchar(30) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `valid` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dev_id_UNIQUE` (`dev_id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=latin1;
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
	IN `param_backend_status` JSON,
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
    FROM ai WHERE `param_dev_id`=`ai`.`dev_id` AND `param_ai_name`=`ai`.`ai_name`;

	IF var_exists_count=0 THEN
		INSERT INTO ai (aiid, ai_name, ai_description, dev_id, is_private, 
						backend_status, client_token, 
                        ui_ai_language, ui_ai_timezone, ui_ai_confidence, ui_ai_personality, ui_ai_voice)
		VALUES (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private,         
				param_backend_status, param_client_token, 
                param_ui_ai_language,
				param_ui_ai_timezone, param_ui_ai_confidence, param_ui_ai_personality, param_ui_ai_voice);
		SET var_named_aiid = `param_aiid`;
	END IF;

    SELECT var_named_aiid AS aiid;

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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntentVariable`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_intent_name VARCHAR(250),
 IN in_entity_name VARCHAR(250),
 IN in_required int(1),
 IN in_n_prompts int,
 IN in_value varchar(250)
 )
BEGIN
	DECLARE update_count INT;

	INSERT INTO `intent_variable` (`intent_id`, `entity_id`, `required`, `n_prompts`, `value`)
    SELECT `intent`.`id`, `entity`.`id`, `in_required`, `in_n_prompts`, `in_value`
	FROM `intent`, `entity`
    WHERE `intent`.`id` =
		(SELECT `id` FROM `intent` WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
		(SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`))
	AND `entity`.`id` =
		(SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_entity_name`=`name`)
	ON DUPLICATE KEY UPDATE
		`required`=`in_required`, `n_prompts`=`in_n_prompts`, `value`=`in_value`,
        `entity_id`= (SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_entity_name`=`name`),
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
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `addUser`(IN `username` VARCHAR(50), IN `email` TINYTEXT, IN `password` VARCHAR(64), IN `password_salt` VARCHAR(250), IN `first_name` VARCHAR(30), IN `last_name` VARCHAR(30), IN `dev_token` VARCHAR(250), IN `plan_id` INT, IN `dev_id` VARCHAR(50), IN `client_token` VARCHAR(250))
    MODIFIES SQL DATA
BEGIN
INSERT INTO `users`(`username`, `email`, `password`, `password_salt`, `first_name`, `last_name`, `dev_token`, `plan_id`, `dev_id`, `client_token`)
VALUES (username, email, password,password_salt, first_name,last_name, dev_token,plan_id, dev_id, client_token);
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
  UPDATE `ai`
    SET `deleted` = 1
    WHERE `dev_id`=`in_dev_id` AND `aiid`=`in_aiid`;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAllAIs`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	UPDATE ai SET `deleted` = 1 WHERE dev_id=param_devid;
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
 IN in_name VARCHAR(250))
BEGIN
	DELETE FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
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
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAi`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE `dev_id`=in_dev_id AND `aiid` = in_aiid AND `deleted` = 0;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiDeepLearningError` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiDeepLearningError`(
	IN `param_aiid` VARCHAR(50))
    READS SQL DATA
    COMMENT 'aiReader@127.0.0.1'
BEGIN
	DECLARE var_learning_error FLOAT;
	SELECT json_extract(backend_status, "$.engines.rnn.training_error" ) INTO var_learning_error
    FROM `ai`
    WHERE `ai`.`aiid`= `param_aiid`;
    
    SELECT IFNULL(var_learning_error, 0) AS 'deep_learning_error';
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiDeepLearningStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiDeepLearningStatus`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT deep_learning_status FROM ai WHERE aiid=param_aiid;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIs`(IN `param_devid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE dev_id=param_devid AND `deleted` = 0;
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
	SELECT `backend_status` 
    FROM `ai` 
    WHERE `aiid`=`param_aiid` AND `dev_id`=param_devid;
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
  SELECT icon FROM botIcon;
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
  SELECT bs.* FROM botStore bs INNER JOIN bot_ai bai WHERE bai.aiid = param_aiid AND bs.dev_id = param_devId;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getClientToken` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`hutoma_caller`@`127.0.0.1` PROCEDURE `getClientToken`(IN `id` INT)
    NO SQL
BEGIN
SELECT `client_token` FROM `users`
WHERE `id`=id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getDebug` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`127.0.0.1` PROCEDURE `getDebug`(IN `l1` INT UNSIGNED)
    READS SQL DATA
    COMMENT 'this is a test procedure to help confirm functionality'
SELECT *
FROM `debug`
LIMIT 0 , l1 ;;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntities`(
 IN in_dev_id VARCHAR(50))
BEGIN
	SELECT `name` FROM `entity` WHERE `in_dev_id`=`dev_id`;
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
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	SELECT `id`, `name`, `topic_in`, `topic_out`
	FROM `intent`
    WHERE `intent`.`name`=`in_name`
    AND `intent`.`id` IN
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
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	SELECT `response`
	FROM `intent_response` WHERE `intent_id` IN
    (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
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
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	SELECT `says`
	FROM `intent_user_says` WHERE `intent_id` IN
    (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
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
 IN in_dev_id VARCHAR(50),
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
    AND `in_aiid` = `intent`.`aiid`
    AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));

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
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_intent_name VARCHAR(250)
 )
BEGIN

	SELECT
    `intent_variable`.`id` AS `id`,
    `entity`.`name` AS `entity_name`,
    `intent_variable`.`required` AS `required`,
    `intent_variable`.`n_prompts` AS `n_prompts`,
    `intent_variable`.`value` AS `value`
	FROM `intent_variable`, `entity`
    WHERE `intent_variable`.`intent_id` =
		(SELECT `id` FROM `intent` WHERE `in_intent_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
		(SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`))
    AND `entity`.`id` =
		(SELECT `id` FROM `entity` WHERE `id` = `intent_variable`.`entity_id` AND `in_dev_id`=`dev_id`);

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
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getPublishedBots`()
    NO SQL
BEGIN
  SELECT * FROM botStore WHERE is_published = 1;
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
CREATE DEFINER=`hutoma_caller`@`127.0.0.1` PROCEDURE `insertResetToken`(IN `token` VARCHAR(40), IN `uid` INT)
    NO SQL
BEGIN
INSERT INTO `resetTokens`(`token`, `uid`) VALUES (token,uid);
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
  IN `param_isPublished` TINYINT(1)
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
     privacy_policy, classification, version, video_link, license_type, is_published)
    VALUES (param_devId, param_aiid, param_name, param_description, param_longDescription, param_alertMessage,
                         param_badge, param_price, param_sample, param_lastUpdate, param_category, param_privacyPolicy, param_classification,
            param_version, param_videoLink, param_licenseType, param_isPublished);

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

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  START TRANSACTION;
    SELECT @botPrice = price FROM botStore WHERE id = param_botId;
    
    INSERT INTO botPurchase (botId, dev_id, price) VALUES (param_botId, param_devId, @botPrice);
  COMMIT;
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

SET var_uuid = uuid();

SET time_now = CONV(CONCAT(SUBSTR(var_uuid, 16, 3),SUBSTR(var_uuid, 10, 4),SUBSTR(var_uuid, 1, 8)), 16, 10) / 10000 - (141427 * 24 * 60 * 60);

INSERT INTO api_rate_limit (dev_id, rate_key, tokens, token_update_time)
VALUES (in_dev_id, in_rate_key, token_ceiling, time_now)
  ON DUPLICATE KEY UPDATE
    tokens = LEAST(tokens + (time_now - token_update_time)/(1000.0 * token_increment_delay_seconds), token_ceiling),
    token_update_time = time_now;

UPDATE api_rate_limit SET
  tokens = tokens-1.0,
  expires = now() + INTERVAL (token_ceiling * token_increment_delay_seconds) SECOND
  WHERE
    dev_id = in_dev_id AND rate_key = in_rate_key AND
    tokens >= 1.0;

SELECT IF(ROW_COUNT()>0, 0, 1) AS rate_limit, tokens FROM api_rate_limit WHERE
dev_id = in_dev_id AND rate_key = in_rate_key;

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
CREATE DEFINER=`botStoreWriter`@`127.0.0.1` PROCEDURE `saveBotIcon`(IN `param_devId` VARCHAR(50), IN `param_botId` INT(11), IN `param_icon` MEDIUMBLOB)
    NO SQL
BEGIN
    REPLACE INTO botIcon (botId, icon)
      SELECT param_botId, param_icon FROM botStore bs WHERE bs.dev_id = param_devId AND bs.id = param_botId;
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
	IN `param_ui_ai_voice` VARCHAR(50))
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
    ui_ai_voice = param_ui_ai_voice
where aiid = param_aiid AND dev_id = param_dev_id;

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
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAiStatus`(
	IN `param_aiid` VARCHAR(50),
    IN `param_devid` VARCHAR(50),
    IN `param_backend_status` JSON)
    READS SQL DATA
BEGIN
	UPDATE `ai` 
    SET `backend_status`=`param_backend_status`
    WHERE `aiid`=`param_aiid` AND `dev_id`=`param_devid`;
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
  IN `param_variables` TEXT, IN `param_isFulFilled` TINYINT(1))
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
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `updateUserLoginAttempts`(IN `userId` INT(11), IN `newAttempt` VARCHAR(15))
    MODIFIES SQL DATA
BEGIN
	UPDATE `users` SET `attempt` = newAttempt WHERE `id`=userId;
END ;;
DELIMITER ;
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

-- Dump completed on 2017-01-09 15:06:24
