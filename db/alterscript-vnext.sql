/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

# Support for API keys
ALTER TABLE `ai` ADD COLUMN `api_keys_desc` JSON DEFAULT NULL AFTER `passthrough_url`;
ALTER TABLE `bot_ai` ADD COLUMN `config` JSON DEFAULT NULL AFTER `dev_id`;

--
-- Table structure for table `bot_ai_config`
--
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

GRANT SELECT ON `hutoma`.`bot_ai_config` TO 'aiReader'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`bot_ai_config` TO 'aiWriter'@'127.0.0.1';

DROP PROCEDURE `getAi`;

DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
READS SQL DATA
  BEGIN

    SELECT
      `id`,
      `aiid`,
      `ai_name`,
      `ai_description`,
      `created_on`,
      `dev_id`,
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
      (SELECT COUNT(`ai_training`.`aiid`)
       FROM `ai_training`
       WHERE `ai_training`.`aiid`=`in_aiid`)
        AS `has_training_file`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;

  END ;;
DELIMITER ;



DROP PROCEDURE `getAIs`;
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

DROP procedure IF EXISTS `getAiSkillConfig`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiSkillConfig`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_bot_id` INT(11))
BEGIN
	SELECT * FROM `bot_ai` WHERE `bot_ai`.`dev_id` = `in_dev_id` AND `bot_ai`.`aiid` = `in_aiid` AND `bot_ai`.`botId` = `in_bot_id`;
END;;

DELIMITER ;
DROP procedure IF EXISTS `getBotsLinkedToAi`;
DELIMITER ;;
CREATE DEFINER=`botStoreReader`@`127.0.0.1` PROCEDURE `getBotsLinkedToAi`(IN `param_devId` VARCHAR(50), IN `param_aiid` VARCHAR(50))
    NO SQL
BEGIN
    SELECT bs.* FROM botStore bs INNER JOIN bot_ai bai ON bai.botId = bs.id WHERE bai.aiid = param_aiid AND bai.dev_id = param_devId AND bai.botId != 0;
END;;

DELIMITER ;
DROP procedure IF EXISTS `getAisLinkedToAi`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAisLinkedToAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50))
BEGIN
	SELECT bai.aiid as 'ai', bs.aiid as 'linked_ai', bs.dev_id as 'linked_ai_devId', ai.ui_ai_confidence as 'minP'
	FROM bot_ai bai INNER JOIN botStore bs ON bs.id = bai.botId INNER JOIN ai ai ON ai.aiid = bs.aiid
	WHERE bai.aiid=param_aiid AND bai.dev_id=param_devId AND botId != 0;
END;;

DELIMITER ;
DROP procedure IF EXISTS `getBotstoreItem`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getBotstoreItem`(
	IN `param_botId` INT)
BEGIN

SELECT bs.*, di.company AS 'dev_company', di.name as 'dev_name', di.email as 'dev_email', di.country as 'dev_country', di.website as 'dev_website', ai.api_keys_desc as 'api_keys_desc'
FROM botStore bs INNER JOIN developerInfo di ON di.dev_id = bs.dev_id INNER JOIN ai ON ai.aiid = bs.aiid WHERE bs.publishing_state=2 AND bs.id = param_botId;

END;;

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
DROP procedure IF EXISTS `setApiKeyDescriptions`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setApiKeyDescriptions`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50),
  IN `in_api_keys_desc` JSON)
    MODIFIES SQL DATA
BEGIN
	UPDATE ai
    SET `api_keys_desc` = `in_api_keys_desc` 
    WHERE aiid = in_aiid AND dev_id = in_dev_id;
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

