-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Oct 03, 2016 at 08:16 AM
-- Server version: 5.5.50-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4.19

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `hutoma`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`aiWriter`@`localhost` PROCEDURE `addAI`(IN `param_aiid` varchar(50), IN `param_ai_name` varchar(50), IN `param_ai_description` varchar(250),IN `param_dev_id` varchar(50), IN `param_is_private` tinyint(1),IN `param_deep_learning_error` double,IN `param_deep_learning_status` tinyint(4),IN `param_shallow_learning_status` int(11),IN `param_ai_status` varchar(50),IN `param_client_token` varchar(250),IN `param_ai_trainingfile` text)
    MODIFIES SQL DATA
BEGIN
	insert into ai (aiid, ai_name, ai_description,dev_id, is_private,deep_learning_error,deep_learning_status,shallow_learning_status,ai_status,client_token,ai_trainingfile)
                                 values (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private, param_deep_learning_error, param_deep_learning_status, param_shallow_learning_status, param_ai_status, param_client_token, param_ai_trainingfile);
END$$

CREATE DEFINER=`entityUser`@`localhost` PROCEDURE `addEntity`(
 IN in_dev_id VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	INSERT INTO `entity` (`dev_id`, `name`) VALUES (`in_dev_id`, `in_name`);
END$$

CREATE DEFINER=`entityUser`@`localhost` PROCEDURE `addEntityValue`(
 IN in_dev_id VARCHAR(50),
 IN in_name VARCHAR(250),
 IN in_value VARCHAR(250))
BEGIN
	INSERT INTO `entity_value` (`entity_id`,`value`)
    SELECT `id`, `in_value` FROM `entity`
    WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `addIntentResponse`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250),
 IN in_response VARCHAR(250))
BEGIN
	INSERT INTO `intent_response` (`intent_id`, `response`)
    SELECT `id`, `in_response` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `addIntentUserSays`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250),
 IN in_says VARCHAR(250))
BEGIN
	INSERT INTO `intent_user_says` (`intent_id`, `says`)
    SELECT `id`, `in_says` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `addIntentVariablePrompt`(
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

END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `addUpdateIntent`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name varchar(250),
 IN in_topic_in varchar(250),
 IN in_topic_out varchar(250)
 )
BEGIN
	INSERT INTO `intent` (`aiid`, `name`, `topic_in`, `topic_out`)
    SELECT `aiid`, `in_name`, `in_topic_in`, `in_topic_out`
    FROM ai
    WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`
    ON DUPLICATE KEY UPDATE `topic_in`=`in_topic_in`, `topic_out`=`in_topic_out`;
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `addUpdateIntentVariable`(
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

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addUser`(IN `username` VARCHAR(50), IN `email` TINYTEXT, IN `password` VARCHAR(64), IN `password_salt` VARCHAR(250), IN `first_name	` VARCHAR(30), IN `last_name` VARCHAR(30), IN `dev_token` VARCHAR(250), IN `plan_id` INT, IN `dev_id` VARCHAR(50), IN `client_token` VARCHAR(250))
    MODIFIES SQL DATA
BEGIN
INSERT INTO `users`(`username`, `email`, `password`, `password_salt`, `first_name`, `last_name`, `dev_token`, `plan_id`, `dev_id`, `client_token`)
VALUES (username, email, password,password_salt, first_name,last_name, dev_token,plan_id, dev_id, client_token);
END$$

CREATE DEFINER=`userTableWriter`@`localhost` PROCEDURE `addUserComplete`(IN `param_username` varchar(50), IN `param_email` tinytext, IN `param_password` varchar(64),IN `param_password_salt` varchar(20), IN `param_name` varchar(30),IN `param_created` datetime,IN `param_attempt` varchar(15),IN `param_dev_token` varchar(250),IN `param_plan_id` int(11),IN `param_dev_id` varchar(50))
    MODIFIES SQL DATA
BEGIN
		insert into users (username, email, password,password_salt,name,created,attempt,dev_token,plan_id,dev_id)
                         values (param_username, param_email, param_password, param_password_salt, param_name, param_created, param_attempt, param_dev_token, param_plan_id, param_dev_id);
END$$

CREATE DEFINER=`aiDeleter`@`localhost` PROCEDURE `deleteAI`(IN `param_aiid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	delete from ai where aiid=param_aiid;
END$$

CREATE DEFINER=`aiDeleter`@`localhost` PROCEDURE `deleteAI_v1`(IN in_dev_id VARCHAR(50), IN `in_aiid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	delete from ai WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`;
END$$

CREATE DEFINER=`aiDeleter`@`localhost` PROCEDURE `deleteAllAIs`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	delete from ai where dev_id=param_devid;
END$$

CREATE DEFINER=`entityUser`@`localhost` PROCEDURE `deleteEntity`(
 IN in_dev_id VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	DELETE FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`;
END$$

CREATE DEFINER=`entityUser`@`localhost` PROCEDURE `deleteEntityValue`(
 IN in_dev_id VARCHAR(50),
 IN in_name VARCHAR(250),
 IN in_value VARCHAR(250))
BEGIN
	DELETE FROM `entity_value` WHERE `in_value`=`value` AND `entity_id`=
    (SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name`);
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `deleteIntent`(
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
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `deleteIntentResponse`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250),
 IN in_response VARCHAR(250))
BEGIN
	DELETE FROM `intent_response`
	WHERE `in_response`=`response` AND `intent_id`=
    (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `deleteIntentUserSays`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250),
 IN in_says VARCHAR(250))
BEGIN
	DELETE FROM `intent_user_says`
	WHERE `in_says`=`says` AND `intent_id`=
    (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `deleteIntentVariable`(
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

END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `deleteIntentVariablePrompt`(
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

END$$

CREATE DEFINER=`userDeleter`@`localhost` PROCEDURE `deleteUser`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	delete from users where dev_id=param_devid;
END$$

CREATE DEFINER=`aiReader`@`localhost` PROCEDURE `getAI`(IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE aiid=param_aiid;
END$$

CREATE DEFINER=`aiReader`@`localhost` PROCEDURE `getAiActive`(IN `param_aiid` varchar(50), IN `param_devid` varchar(50))
    READS SQL DATA
BEGIN
	SELECT NNActive FROM ai WHERE dev_id=param_devid AND aiid=param_aiid;
END$$

CREATE DEFINER=`aiReader`@`localhost` PROCEDURE `getAIs`(IN `param_devid` varchar(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE dev_id=param_devid;
END$$

CREATE DEFINER=`aiReader`@`localhost` PROCEDURE `getAI_v1`(IN in_dev_id VARCHAR(50), IN `in_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
	SELECT * FROM ai WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`;
END$$

CREATE DEFINER=`chatlogReader`@`localhost` PROCEDURE `getAnswer`(IN `param_qid` int(11))
    READS SQL DATA
BEGIN
	SELECT answer FROM chatlog WHERE id=param_qid;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getDebug`(IN `l1` INT UNSIGNED)
    READS SQL DATA
    COMMENT 'this is a test procedure to help confirm functionality'
SELECT *
FROM `debug`
LIMIT 0 , l1$$

CREATE DEFINER=`domainsReader`@`localhost` PROCEDURE `getDomains`()
    READS SQL DATA
BEGIN
  SELECT * FROM `domains`;
END$$

CREATE DEFINER=`domainsReader`@`localhost` PROCEDURE `getDomainsAndUserActiveDomains`(IN `param_dev_id` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    READS SQL DATA
BEGIN
  SELECT * FROM `domains` AS d LEFT OUTER JOIN ( SELECT * FROM `userAIDomains` WHERE `dev_id` = param_dev_id AND  `aiid`= param_aiid ) AS u ON u.dom_id = d.dom_id;
END$$

CREATE DEFINER=`entityUser`@`localhost` PROCEDURE `getEntities`(
 IN in_dev_id VARCHAR(50))
BEGIN
	SELECT `name` FROM `entity` WHERE `in_dev_id`=`dev_id`;
END$$

CREATE DEFINER=`entityUser`@`localhost` PROCEDURE `getEntityValues`(
 IN in_dev_id VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	SELECT `entity_value`.`value` FROM `entity`,`entity_value`
    WHERE `entity`.`dev_id`=`in_dev_id`
    AND `entity`.`name`=`in_name`
    AND `entity`.`id`=`entity_value`.`entity_id`;
END$$

CREATE DEFINER=`integrReader`@`localhost` PROCEDURE `getIntegrations`()
    READS SQL DATA
BEGIN
  SELECT * FROM `integrations`;
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntentIDs`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50)
 )
BEGIN

	SELECT `intent`.`id` from `intent`, `ai`
    WHERE `ai`.`dev_id` = `in_dev_id`
    AND `ai`.`aiid` = `in_aiid`
	AND `intent`.`aiid` = `in_aiid`;

END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntentResponses`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	SELECT `response`
	FROM `intent_response` WHERE `intent_id` IN
    (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntents`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50))
BEGIN
	SELECT `id`, `name`, `topic_in`, `topic_out`
	FROM `intent` WHERE `intent`.`id` IN
		(SELECT `intent`.`id` from `intent`, `ai`
		WHERE `ai`.`dev_id` = `in_dev_id`
		AND `ai`.`aiid` = `in_aiid`
		AND `intent`.`aiid` = `in_aiid`);
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntentUserSays`(
 IN in_dev_id VARCHAR(50),
 IN in_aiid VARCHAR(50),
 IN in_name VARCHAR(250))
BEGIN
	SELECT `says`
	FROM `intent_user_says` WHERE `intent_id` IN
    (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
    (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntentVariableIDs`(
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

END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntentVariablePrompts`(
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

END$$

CREATE DEFINER=`intentUser`@`localhost` PROCEDURE `getIntentVariables`(
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

END$$

CREATE DEFINER=`userTableReader`@`localhost` PROCEDURE `getUser`(IN `uname` VARCHAR(50), IN `checkEmail` BOOLEAN)
    READS SQL DATA
BEGIN
IF checkEmail THEN
  SELECT `email`, `id`, `password`, `password_salt`, `attempt` FROM users WHERE `username`=uname OR `email`=uname ORDER BY `id` LIMIT 1;
ELSE
  SELECT `email`, `id`, `password`, `attempt` FROM users WHERE `username`=uname ORDER BY `id` LIMIT 1;
END IF;
END$$

CREATE DEFINER=`userTableReader`@`localhost` PROCEDURE `getUserById`(IN `idValue` INT(11), IN `columnValues` TINYTEXT)
    READS SQL DATA
BEGIN
SET @s=CONCAT('SELECT ',columnValues,' FROM users WHERE `id`=', idValue, ' ORDER BY `id` LIMIT 1');
PREPARE stmt1 FROM @s;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;


END$$

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
END$$

CREATE DEFINER=`chatlogWriter`@`localhost` PROCEDURE `insertQuestion`(IN `param_dev_id` VARCHAR(50), IN `param_message_from` VARCHAR(50), IN `param_message_to` VARCHAR(50), IN `param_question` TEXT)
    MODIFIES SQL DATA
BEGIN
	insert into chatlog (dev_id, message_from, message_to, question)
                     values (param_dev_id, param_message_from, param_message_to, param_question);
	select last_insert_id();
END$$

CREATE DEFINER=`hutoma_caller`@`localhost` PROCEDURE `insertResetToken`(IN `token` VARCHAR(40), IN `uid` INT)
    NO SQL
BEGIN
INSERT INTO `resetTokens`(`token`, `uid`) VALUES (token,uid);
END$$

CREATE DEFINER=`domainsWriter`@`localhost` PROCEDURE `insertUserActiveDomain`(IN `param_dev_id` VARCHAR(50), IN `param_aiid` VARCHAR(50), IN `param_dom_id` VARCHAR(50), IN `param_active` BOOLEAN)
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
END$$

CREATE DEFINER=`rateLimiter`@`localhost` PROCEDURE `rate_limit_check`(IN `in_dev_id` VARCHAR(50) CHARSET utf8, IN `in_rate_key` VARCHAR(50) CHARSET utf8, IN `token_ceiling` FLOAT, IN `token_increment_delay_seconds` FLOAT)
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

END$$

CREATE DEFINER=`aiWriter`@`localhost` PROCEDURE `updateAI`(IN `param_aiid` VARCHAR(50), IN `param_description` VARCHAR(250), IN `param_private` TINYINT(1) UNSIGNED)
    MODIFIES SQL DATA
BEGIN
	UPDATE ai SET
		ai_description=param_description,
		is_private=param_private
	WHERE aiid=param_aiid;
END$$

CREATE DEFINER=`aiWriter`@`localhost` PROCEDURE `updateTrainingData`(IN `param_aiid` varchar(50), IN `param_ai_trainingfile` text)
    MODIFIES SQL DATA
BEGIN
	update ai set ai_trainingfile=param_ai_trainingfile where aiid=param_aiid;
END$$

CREATE DEFINER=`userTableWriter`@`localhost` PROCEDURE `updateUserLoginAttempts`(IN `userId` INT(11), IN `newAttempt` VARCHAR(15))
    MODIFIES SQL DATA
BEGIN
	UPDATE `users` SET `attempt` = newAttempt WHERE `id`=userId;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `ai`
--

CREATE TABLE IF NOT EXISTS `ai` (
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `aiid_UNIQUE` (`aiid`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1034 ;

-- --------------------------------------------------------

--
-- Table structure for table `ai_memory`
--

CREATE TABLE IF NOT EXISTS `ai_memory` (
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

-- --------------------------------------------------------

--
-- Table structure for table `api_rate_limit`
--

CREATE TABLE IF NOT EXISTS `api_rate_limit` (
  `dev_id` varchar(50) NOT NULL,
  `rate_key` varchar(50) NOT NULL,
  `tokens` float DEFAULT NULL,
  `token_update_time` bigint(20) DEFAULT NULL,
  `expires` datetime DEFAULT NULL,
  PRIMARY KEY (`dev_id`,`rate_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `chatlog`
--

CREATE TABLE IF NOT EXISTS `chatlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_from` varchar(50) NOT NULL,
  `message_to` varchar(50) NOT NULL,
  `question` text NOT NULL,
  `timestap` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `answered` int(11) NOT NULL DEFAULT '0',
  `answer` varchar(2000) DEFAULT NULL,
  `dev_id` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2327 ;

-- --------------------------------------------------------

--
-- Table structure for table `debug`
--

CREATE TABLE IF NOT EXISTS `debug` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=840 ;

-- --------------------------------------------------------

--
-- Table structure for table `devplan`
--

CREATE TABLE IF NOT EXISTS `devplan` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `maxai` int(11) NOT NULL,
  `monthlycalls` int(11) NOT NULL,
  `maxmem` int(11) NOT NULL,
  `maxtraining` int(11) NOT NULL DEFAULT '0',
  `plan_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

-- --------------------------------------------------------

--
-- Table structure for table `domains`
--

CREATE TABLE IF NOT EXISTS `domains` (
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

-- --------------------------------------------------------

--
-- Table structure for table `entities`
--

CREATE TABLE IF NOT EXISTS `entities` (
  `entity_id` varchar(50) NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `entity_name` varchar(50) NOT NULL,
  `entity_key` varchar(250) NOT NULL,
  PRIMARY KEY (`entity_id`,`entity_name`,`entity_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `entity`
--

CREATE TABLE IF NOT EXISTS `entity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dev_id` varchar(50) NOT NULL,
  `name` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dev_id` (`dev_id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `entity_value`
--

CREATE TABLE IF NOT EXISTS `entity_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entity_id` int(11) NOT NULL,
  `value` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `entity_id` (`entity_id`,`value`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `integrations`
--

CREATE TABLE IF NOT EXISTS `integrations` (
  `int_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(150) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `available` tinyint(1) NOT NULL,
  PRIMARY KEY (`int_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `intent`
--

CREATE TABLE IF NOT EXISTS `intent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `aiid` varchar(50) NOT NULL,
  `name` varchar(250) NOT NULL,
  `topic_in` varchar(250) DEFAULT NULL,
  `topic_out` varchar(250) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `aiid` (`aiid`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `intents`
--

CREATE TABLE IF NOT EXISTS `intents` (
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

-- --------------------------------------------------------

--
-- Table structure for table `intent_response`
--

CREATE TABLE IF NOT EXISTS `intent_response` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_id` int(11) NOT NULL,
  `response` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_id` (`intent_id`,`response`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `intent_user_says`
--

CREATE TABLE IF NOT EXISTS `intent_user_says` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_id` int(11) NOT NULL,
  `says` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_id` (`intent_id`,`says`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `intent_variable`
--

CREATE TABLE IF NOT EXISTS `intent_variable` (
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
  KEY `entity_id` (`entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `intent_variables`
--

CREATE TABLE IF NOT EXISTS `intent_variables` (
  `intent_id` varchar(50) NOT NULL,
  `entity_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `response` text NOT NULL,
  `dev_id` varchar(50) NOT NULL,
  `nprompts` int(11) NOT NULL DEFAULT '3',
  `required` tinyint(4) NOT NULL DEFAULT '0',
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

-- --------------------------------------------------------

--
-- Table structure for table `intent_variable_prompt`
--

CREATE TABLE IF NOT EXISTS `intent_variable_prompt` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `intent_variable_id` int(11) NOT NULL,
  `prompt` varchar(250) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `intent_variable_id` (`intent_variable_id`,`prompt`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `resetTokens`
--

CREATE TABLE IF NOT EXISTS `resetTokens` (
  `token` varchar(40) NOT NULL COMMENT 'The Unique Token Generated',
  `uid` int(11) NOT NULL COMMENT 'The User Id',
  `requested` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `userAIDomains`
--

CREATE TABLE IF NOT EXISTS `userAIDomains` (
  `dev_id` varchar(50) NOT NULL,
  `aiid` varchar(50) NOT NULL,
  `dom_id` varchar(50) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `created_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dev_id`,`aiid`,`dom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `dev_id_UNIQUE` (`dev_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `entity`
--
ALTER TABLE `entity`
  ADD CONSTRAINT `entity_ibfk_1` FOREIGN KEY (`dev_id`) REFERENCES `users` (`dev_id`) ON DELETE CASCADE;

--
-- Constraints for table `entity_value`
--
ALTER TABLE `entity_value`
  ADD CONSTRAINT `entity_value_ibfk_1` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `intent`
--
ALTER TABLE `intent`
  ADD CONSTRAINT `intent_ibfk_1` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE;

--
-- Constraints for table `intent_response`
--
ALTER TABLE `intent_response`
  ADD CONSTRAINT `intent_response_ibfk_1` FOREIGN KEY (`intent_id`) REFERENCES `intent` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `intent_user_says`
--
ALTER TABLE `intent_user_says`
  ADD CONSTRAINT `intent_user_says_ibfk_1` FOREIGN KEY (`intent_id`) REFERENCES `intent` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `intent_variable`
--
ALTER TABLE `intent_variable`
  ADD CONSTRAINT `intent_variable_ibfk_1` FOREIGN KEY (`intent_id`) REFERENCES `intent` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `intent_variable_ibfk_2` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `intent_variable_prompt`
--
ALTER TABLE `intent_variable_prompt`
  ADD CONSTRAINT `intent_variable_prompt_ibfk_1` FOREIGN KEY (`intent_variable_id`) REFERENCES `intent_variable` (`id`) ON DELETE CASCADE;

DELIMITER $$
--
-- Events
--
CREATE DEFINER=`admin`@`localhost` EVENT `clean_rate_limit` ON SCHEDULE EVERY 1 DAY STARTS '2016-09-14 13:50:06' ON COMPLETION PRESERVE ENABLE DO BEGIN

DELETE FROM  `api_rate_limit` WHERE expires < NOW( );

END$$

DELIMITER ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
