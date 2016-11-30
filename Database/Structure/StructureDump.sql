CREATE DATABASE  IF NOT EXISTS `hutoma` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `hutoma`;
-- MySQL dump 10.13  Distrib 5.7.12, for osx10.9 (x86_64)
--
-- Host: 52.44.215.190    Database: hutoma
-- ------------------------------------------------------
-- Server version	5.5.52-0ubuntu0.14.04.1

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
  `deep_learning_error` double DEFAULT '-1',
  `deep_learning_status` varchar(20) DEFAULT 'training_not_started',
  `shallow_learning_status` int(11) DEFAULT '0',
  `ai_status` varchar(50) DEFAULT 'training_not_started',
  `client_token` varchar(250) NOT NULL,
  `ai_trainingfile` text,
  `internal_status` tinyint(4) NOT NULL DEFAULT '1',
  `shallow_learning_error` double DEFAULT '0',
  `NNActive` int(11) NOT NULL DEFAULT '0',
  `model_files_available` int(11) NOT NULL DEFAULT '0',
  `dl_debug` varchar(500) DEFAULT 'no debug info yet',
  `ai_language` varchar(10) DEFAULT 'en-US',
  `ai_timezone` varchar(50) DEFAULT 'UTC',
  `ai_confidence` double DEFAULT NULL,
  `ai_personality` tinyint(4) DEFAULT '0',
  `ai_voice` int(11) DEFAULT '0',
  `numberOfActivations` int(11) NOT NULL DEFAULT '0',
  `rating` float DEFAULT NULL,
  `isBanned` tinyint(4) DEFAULT NULL,
  `licenceType` int(11) DEFAULT NULL,
  `licenceFee` float DEFAULT NULL,
  `iconPath` varchar(250) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `aiid_UNIQUE` (`aiid`)
) ENGINE=InnoDB AUTO_INCREMENT=1637 DEFAULT CHARSET=latin1;
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
-- Table structure for table `ai_mesh`
--

DROP TABLE IF EXISTS `ai_mesh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ai_mesh` (
  `dev_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `aiid_mesh` varchar(50) NOT NULL,
  PRIMARY KEY (`dev_id`,`aiid`,`aiid_mesh`),
  UNIQUE KEY `aiid` (`aiid`,`aiid_mesh`)
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
-- Table structure for table `botStore`
--

DROP TABLE IF EXISTS `botStore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `botStore` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `aiid` varchar(50) NOT NULL,
  `published` tinyint(1) NOT NULL DEFAULT '1',
  `publisher_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_2` (`aiid`),
  KEY `id_3` (`publisher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chatlog`
--

DROP TABLE IF EXISTS `chatlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chatlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_from` varchar(50) NOT NULL,
  `message_to` varchar(50) NOT NULL,
  `question` text NOT NULL,
  `timestap` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `answered` int(11) NOT NULL DEFAULT '0',
  `answer` varchar(2000) DEFAULT NULL,
  `dev_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4064 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=402 DEFAULT CHARSET=latin1;
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
-- Table structure for table `entities_TOBE_DELETED`
--

DROP TABLE IF EXISTS `entities_TOBE_DELETED`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entities_TOBE_DELETED` (
  `entity_id` varchar(50) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `entity_name` varchar(50) NOT NULL,
  `entity_key` varchar(250) NOT NULL,
  PRIMARY KEY (`entity_id`,`entity_name`,`entity_key`)
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
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intent_variables_TOBE_DELETED`
--

DROP TABLE IF EXISTS `intent_variables_TOBE_DELETED`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intent_variables_TOBE_DELETED` (
  `intent_id` varchar(50) NOT NULL,
  `entity_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `response` text NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `nprompts` int(11) NOT NULL DEFAULT '3',
  `required` tinyint(4) NOT NULL DEFAULT '0',
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intents_TOBE_DELETED`
--

DROP TABLE IF EXISTS `intents_TOBE_DELETED`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intents_TOBE_DELETED` (
  `intent_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `topic_in` varchar(50) NOT NULL,
  `topic_out` varchar(50) NOT NULL,
  `training_data` text NOT NULL,
  `response` text NOT NULL,
  `intent_name` varchar(50) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  PRIMARY KEY (`intent_id`,`aiid`,`intent_name`,`dev_id`)
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
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=latin1;
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
/*!50003 DROP PROCEDURE IF EXISTS `addAI` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addAI`(IN `param_aiid` varchar(50), IN `param_ai_name` varchar(50), IN `param_ai_description` varchar(250),IN `param_dev_id` varchar(50), IN `param_is_private` tinyint(1),IN `param_deep_learning_error` double,IN `param_deep_learning_status` tinyint(4),IN `param_shallow_learning_status` int(11),IN `param_ai_status` varchar(50),IN `param_client_token` varchar(250),IN `param_ai_trainingfile` text)
    MODIFIES SQL DATA
BEGIN
	insert into ai (aiid, ai_name, ai_description,dev_id, is_private,deep_learning_error,deep_learning_status,shallow_learning_status,ai_status,client_token,ai_trainingfile)
                                 values (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private, param_deep_learning_error, param_deep_learning_status, param_shallow_learning_status, param_ai_status, param_client_token, param_ai_trainingfile);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `addAI_v1` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `addAI_v1`(IN `param_aiid` VARCHAR(50), IN `param_ai_name` VARCHAR(50), IN `param_ai_description` VARCHAR(250), IN `param_dev_id` VARCHAR(50), IN `param_is_private` TINYINT(1), IN `param_deep_learning_error` DOUBLE, IN `param_deep_learning_status` TINYINT(4), IN `param_shallow_learning_status` INT(11), IN `param_ai_status` VARCHAR(50), IN `param_client_token` VARCHAR(250), IN `param_ai_trainingfile` TEXT, IN `param_ai_language` VARCHAR(10), IN `param_ai_timezone` VARCHAR(50), IN `param_ai_confidence` DOUBLE, IN `param_ai_personality` BOOLEAN, IN `param_ai_voice` INT(11))
    MODIFIES SQL DATA
BEGIN

	insert into ai (aiid, ai_name, ai_description,dev_id, is_private,deep_learning_error,deep_learning_status,
					shallow_learning_status,ai_status,client_token,ai_trainingfile,ai_language,
                    ai_timezone,ai_confidence,ai_personality,ai_voice)
	values (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private, param_deep_learning_error, param_deep_learning_status,
			param_shallow_learning_status, param_ai_status, param_client_token, param_ai_trainingfile,param_ai_language,
            param_ai_timezone,param_ai_confidence,param_ai_personality,param_ai_voice);

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
/*!50003 DROP PROCEDURE IF EXISTS `addMesh` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`hutoma_caller`@`127.0.0.1` PROCEDURE `addMesh`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50), IN `in_aiid_mesh` VARCHAR(50))
    NO SQL
BEGIN
  INSERT INTO ai_mesh (dev_id,aiid,aiid_mesh)
  VALUES (in_dev_id,in_aiid, in_aiid_mesh);
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
CREATE DEFINER=`root`@`127.0.0.1` PROCEDURE `addUser`(IN `username` VARCHAR(50), IN `email` TINYTEXT, IN `password` VARCHAR(64), IN `password_salt` VARCHAR(250), IN `first_name` VARCHAR(30), IN `last_name` VARCHAR(30), IN `dev_token` VARCHAR(250), IN `plan_id` INT, IN `dev_id` VARCHAR(50), IN `client_token` VARCHAR(250))
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
/*!50003 DROP PROCEDURE IF EXISTS `deleteAI` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAI`(IN `param_aiid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	delete from ai where aiid=param_aiid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `deleteAI_v1` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAI_v1`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
	delete from ai WHERE dev_id=`in_dev_id` AND `aiid`=`in_aiid`;
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
	delete from ai where dev_id=param_devid;
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
/*!50003 DROP PROCEDURE IF EXISTS `deleteAllMesh` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`mesh_writer`@`127.0.0.1` PROCEDURE `deleteAllMesh`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50))
    NO SQL
BEGIN
DELETE FROM ai_mesh
WHERE dev_id=in_dev_id AND aiid=in_aiid;
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
/*!50003 DROP PROCEDURE IF EXISTS `deleteMesh` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`mesh_writer`@`127.0.0.1` PROCEDURE `deleteMesh`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50), IN `in_aiid_mesh` VARCHAR(50))
    NO SQL
BEGIN
DELETE FROM ai_mesh
WHERE dev_id=in_dev_id AND aiid=in_aiid AND aiid_mesh = in_aiid_mesh;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `existsAiTrainingFile`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	   SELECT
	CASE WHEN ai_trainingfile is null
		 THEN false
		 ELSE true END as ai_trainingfile
		 FROM ai
        WHERE aiid=param_aiid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAI` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAI`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE aiid=param_aiid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAiActive` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiActive`(IN `param_aiid` varchar(50), IN `param_devid` varchar(50))
    READS SQL DATA
BEGIN
	SELECT NNActive FROM ai WHERE dev_id=param_devid AND aiid=param_aiid;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`127.0.0.1` PROCEDURE `getAiDeepLearningError`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
    COMMENT 'aiReader@127.0.0.1'
BEGIN
	SELECT deep_learning_error FROM ai WHERE aiid=param_aiid;
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
	SELECT * FROM ai WHERE dev_id=param_devid;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiStatus`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT ai_status FROM ai WHERE aiid=param_aiid;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiTrainingFile`(IN `param_aiid` varchar(50))
    READS SQL DATA
BEGIN

	SELECT ai_trainingfile FROM ai WHERE aiid=param_aiid;
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
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiVoice`(IN `param_aiid` INT)
    NO SQL
BEGIN
	SELECT ai_voice FROM ai WHERE aiid=param_aiid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAI_v1` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAI_v1`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE `dev_id`=in_dev_id AND `aiid`=in_aiid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getAnswer` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`chatlogReader`@`127.0.0.1` PROCEDURE `getAnswer`(IN `param_qid` int(11))
    READS SQL DATA
BEGIN
	SELECT answer FROM chatlog WHERE id=param_qid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `getBotsInStore` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getBotsInStore`()
    NO SQL
SELECT ai.aiid as dom_id, ai_name as name, ai_description as description,iconPath as icon, color, published
FROM ai
INNER JOIN botStore
on ai.aiid = botStore.aiid ;;
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
/*!50003 DROP PROCEDURE IF EXISTS `getMesh` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`mesh_reader`@`127.0.0.1` PROCEDURE `getMesh`(IN `in_dev_id` VARCHAR(50), IN `in_aiid` VARCHAR(50))
    NO SQL
BEGIN
SELECT ai_mesh.aiid_mesh, ai.*
FROM ai
INNER JOIN ai_mesh ON ai.aiid = ai_mesh.aiid
WHERE ai.dev_id=in_dev_id AND ai.aiid = in_aiid;
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
/*!50003 DROP PROCEDURE IF EXISTS `insertQuestion` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`chatlogWriter`@`127.0.0.1` PROCEDURE `insertQuestion`(IN `param_dev_id` VARCHAR(50), IN `param_message_from` VARCHAR(50), IN `param_message_to` VARCHAR(50), IN `param_question` TEXT)
    MODIFIES SQL DATA
BEGIN
	insert into chatlog (dev_id, message_from, message_to, question)
                     values (param_dev_id, param_message_from, param_message_to, param_question);
	select last_insert_id();
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `insertQuestion_v1` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`chatlogWriter`@`127.0.0.1` PROCEDURE `insertQuestion_v1`(
	IN `in_devid` VARCHAR(50),
    IN `in_chatid` VARCHAR(50),
    IN `in_aiid` VARCHAR(50),
    IN `in_question` TEXT)
    MODIFIES SQL DATA
BEGIN
	DECLARE var_found INT;
	DECLARE var_ai_status VARCHAR(50);
    DECLARE var_lastid LONG;
    DECLARE var_failed_status TINYINT;

    SELECT count(ai.aiid), ai.ai_status INTO var_found, var_ai_status FROM ai where ai.aiid = in_aiid AND ai.dev_id = in_devid LIMIT 1;

	SET var_lastid = -1;
    IF var_found>0 THEN
		SET var_failed_status =
			IF (var_ai_status="training_completed"
			OR var_ai_status="training_in_progress"
			OR var_ai_status="training_stopped_maxtime"
			OR var_ai_status="training_stopped",
			0, 1);

		IF NOT var_failed_status
		THEN
			INSERT INTO chatlog (dev_id, message_from, message_to, question)
			VALUES (in_devid, in_chatid, in_aiid, in_question);
			SET var_lastid = last_insert_id();
		END IF;
	END IF;

	SELECT var_ai_status AS ai_status, var_lastid AS id, var_failed_status AS failed_status;

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
/*!50003 DROP PROCEDURE IF EXISTS `rate_limit_check` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`rateLimiter`@`127.0.0.1` PROCEDURE `rate_limit_check`(IN `in_dev_id` VARCHAR(50) CHARSET utf8, IN `in_rate_key` VARCHAR(50) CHARSET utf8, IN `token_ceiling` FLOAT, IN `token_increment_delay_seconds` FLOAT)
    MODIFIES SQL DATA
BEGIN

DECLARE time_now BIGINT;

DECLARE var_uuid VARCHAR(32);

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
/*!50003 DROP PROCEDURE IF EXISTS `setRnnStatus` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`rnnWriter`@`%` PROCEDURE `setRnnStatus`(IN `status` INT, IN `param_devid` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
	UPDATE `ai`
    SET `NNActive`= status
	WHERE `aiid` = param_aiid AND `dev_id` = param_devid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAI` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAI`(IN `param_aiid` VARCHAR(50), IN `param_description` VARCHAR(250), IN `param_private` TINYINT(1) UNSIGNED)
    MODIFIES SQL DATA
BEGIN
	UPDATE ai SET
		ai_description=param_description,
		is_private=param_private
	WHERE aiid=param_aiid;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAI_v1` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAI_v1`(IN `param_aiid` VARCHAR(50), IN `param_ai_description` VARCHAR(250), IN `param_dev_id` VARCHAR(50), IN `param_is_private` TINYINT(1), IN `param_ai_language` VARCHAR(10), IN `param_ai_timezone` VARCHAR(50), IN `param_ai_confidence` DOUBLE, IN `param_ai_personality` TINYINT(4), IN `param_ai_voice` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN

update ai
set
    ai_description = param_ai_description,
    is_private = param_is_private,
    ai_language = param_ai_language,
    ai_timezone = param_ai_timezone,
    ai_confidence = param_ai_confidence,
    ai_personality = param_ai_personality,
    ai_voice = param_ai_voice
where aiid = param_aiid AND dev_id = param_dev_id;

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
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `updateMemoryIntent`(IN `param_name` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_chatId` VARCHAR(50), IN `param_variables` TEXT)
BEGIN
	INSERT INTO memoryIntent (aiid, chatId, name, variables, lastAccess)
		VALUES(param_aiid, param_chatId, param_name, param_variables, NOW())

	ON DUPLICATE KEY UPDATE variables = param_variables, lastAccess = NOW();
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateTrainingData` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateTrainingData`(IN `param_aiid` varchar(50), IN `param_ai_trainingfile` text)
    MODIFIES SQL DATA
BEGIN
	update ai set ai_trainingfile=param_ai_trainingfile where aiid=param_aiid;
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

-- Dump completed on 2016-10-29 23:42:51
