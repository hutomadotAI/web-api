-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: 54.83.145.18    Database: hutoma
-- ------------------------------------------------------
-- Server version	5.5.49-0ubuntu0.14.04.1

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
  `deep_learning_status` varchar(20) DEFAULT 'not started',
  `shallow_learning_status` int(11) DEFAULT '0',
  `ai_status` varchar(50) DEFAULT 'created',
  `client_token` varchar(250) NOT NULL,
  `ai_trainingfile` text,
  `internal_status` tinyint(4) NOT NULL DEFAULT '1',
  `shallow_learning_error` double DEFAULT '0',
  `NNActive` int(11) NOT NULL DEFAULT '0',
  `model_files_available` int(11) NOT NULL DEFAULT '0',
  `dl_debug` varchar(500) DEFAULT 'no debug info yet',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=542 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=187 DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB AUTO_INCREMENT=536 DEFAULT CHARSET=latin1;
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
-- Table structure for table `entities`
--

DROP TABLE IF EXISTS `entities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entities` (
  `entity_id` varchar(50) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `entity_name` varchar(50) NOT NULL,
  `entity_keys` text NOT NULL,
  `prompts` text NOT NULL,
  `max_prompts` int(11) NOT NULL,
  `required` tinyint(1) NOT NULL,
  PRIMARY KEY (`entity_id`,`dev_id`,`entity_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `integrations`
--

DROP TABLE IF EXISTS `integrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `integrations` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(150) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `available` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `intents`
--

DROP TABLE IF EXISTS `intents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `intents` (
  `intent_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `topic_in` varchar(50) NOT NULL,
  `topic_out` varchar(50) NOT NULL,
  `training_data` text NOT NULL,
  `response` text NOT NULL,
  `intent_name` varchar(50) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `entity_list` text NOT NULL,
  PRIMARY KEY (`intent_id`,`aiid`,`intent_name`,`dev_id`)
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
  `requested` varchar(20) NOT NULL COMMENT 'The Date when token was created'
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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` tinytext NOT NULL,
  `password` varchar(64) NOT NULL,
  `password_salt` varchar(20) NOT NULL,
  `name` varchar(30) NOT NULL,
  `created` datetime NOT NULL,
  `attempt` varchar(15) NOT NULL DEFAULT '0',
  `dev_token` varchar(250) NOT NULL,
  `plan_id` int(11) NOT NULL DEFAULT '0',
  `dev_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=MyISAM AUTO_INCREMENT=441 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'hutoma'
--

--
-- Dumping routines for database 'hutoma'
--
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
CREATE DEFINER=`userTableWriter`@`localhost` PROCEDURE `addUser`(IN `emailValue` TINYTEXT, IN `passwordValue` VARCHAR(64), IN `pwSalt` VARCHAR(20), IN `uName` VARCHAR(50), IN `fullName` VARCHAR(30), IN `creationDate` DATETIME)
    MODIFIES SQL DATA
BEGIN
  INSERT INTO users (`email`, `password`, `password_salt`, `username`, `name`, `created`) VALUES (emailValue, passwordValue, pwSalt, uName, fullName, creationDate);
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
CREATE DEFINER=`root`@`localhost` PROCEDURE `getDebug`(IN `l1` INT UNSIGNED)
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
CREATE DEFINER=`userTableReader`@`localhost` PROCEDURE `getUser`(IN `uname` VARCHAR(50), IN `checkEmail` BOOLEAN)
    READS SQL DATA
BEGIN
IF checkEmail THEN
  SELECT `id`, `password`, `password_salt`, `attempt` FROM users WHERE `username`=uname OR `email`=uname ORDER BY `id` LIMIT 1;
ELSE
  SELECT `id`, `password`, `password_salt`, `attempt` FROM users WHERE `username`=uname ORDER BY `id` LIMIT 1;
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
CREATE DEFINER=`userTableReader`@`localhost` PROCEDURE `getUserById`(IN `idValue` INT(11), IN `columnValues` TINYTEXT)
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
CREATE DEFINER=`userTableReader`@`localhost` PROCEDURE `getUserId`(IN `nameOfUser` VARCHAR(50) CHARSET latin1, IN `checkEmail` BOOLEAN)
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
CREATE DEFINER=`root`@`localhost` PROCEDURE `insertUserActiveDomain`(IN `param_dev_id` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_dom_id` VARCHAR(50), IN `param_active` BOOLEAN)
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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-08-11 19:57:44
