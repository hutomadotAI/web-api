/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `botStore` ADD COLUMN featured tinyint(1) NOT NULL DEFAULT 0 AFTER `botIcon`;
ALTER TABLE intent_variable DROP KEY intent_id, ADD KEY intent_id(intent_id);
ALTER TABLE `intent_variable` ADD COLUMN `label` varchar(50) DEFAULT NULL AFTER `dummy`;


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

DROP PROCEDURE IF EXISTS getIntentVariables;
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

DROP PROCEDURE IF EXISTS addUpdateIntentVariable;
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



DROP PROCEDURE IF EXISTS `deleteIntegration`;
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

  END;;
DELIMITER ;

DROP PROCEDURE IF EXISTS `checkIntegrationUser`;
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
    
  END;;
DELIMITER ;

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`ai_integration` TO 'aiReader'@'127.0.0.1';

INSERT INTO `hutoma`.`entity` (`dev_id`, `name`, `created`, `isSystem`, `isPersistent`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.any', '2017-05-12 00:00:00', '1', '0');

