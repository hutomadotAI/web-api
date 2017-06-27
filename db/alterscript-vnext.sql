/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `botStore` ADD COLUMN featured tinyint(1) NOT NULL DEFAULT 0 AFTER `botIcon`;


DROP PROCEDURE IF EXISTS `getAisLinkedToAi`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAisLinkedToAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50))
BEGIN
	SELECT bai.aiid as 'ai', bs.aiid as 'linked_ai', bai.dev_id as 'linked_ai_devId', ai.ui_ai_confidence as 'minP'
	FROM bot_ai bai INNER JOIN botStore bs ON bs.id = bai.botId INNER JOIN ai ai ON ai.aiid = bs.aiid
	WHERE bai.aiid=param_aiid AND bai.dev_id=param_devId;
END ;;
DELIMITER ;


DROP TABLE IF EXISTS `hutoma`.`ai_integration`;
CREATE TABLE `hutoma`.`ai_integration` (
  `aiid` varchar(50) NOT NULL,
  `integration` varchar(50) NOT NULL,
  `integrated_resource` varchar(250) NULL,
  `integrated_userid` varchar(250) NULL,
  `data` JSON DEFAULT NULL,
  `status` varchar(1024) DEFAULT NULL,
  `active` TINYINT DEFAULT 0,
  `chat_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`aiid`,`integration`),
  INDEX (`integration`, `integrated_resource`),
  CONSTRAINT `fk_integration_aiid` FOREIGN KEY (`aiid`) 
  REFERENCES `ai` (`aiid`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP PROCEDURE IF EXISTS `updateAiIntegration`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50),
  IN `in_integrated_resource` VARCHAR(250),
  IN `in_integrated_userid` VARCHAR(250),
  IN `in_data` JSON,
  IN `in_status` VARCHAR(1024),
  IN `in_active` TINYINT)
BEGIN

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
  END;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `getAiIntegration`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiIntegration`(
  IN `in_aiid` VARCHAR(50),
  IN `in_devid` VARCHAR(50),
  IN `in_integration` VARCHAR(50))
BEGIN

SELECT `integrated_resource`, `integrated_userid`, `data`, `status`, `active`
FROM `hutoma`.`ai_integration`
INNER JOIN `ai` ON `ai`.`aiid` = `ai_integration`.`aiid`
WHERE `ai`.`dev_id` = `in_devid`
AND `ai_integration`.`aiid`=`in_aiid` 
AND `integration`=`in_integration`;
    
  END;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `getIntegratedResource`;
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

  END;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `updateIntegrationStatus`;
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
 
END;;
DELIMITER ;

GRANT SELECT, INSERT, UPDATE ON `hutoma`.`ai_integration` TO 'aiReader'@'127.0.0.1';
