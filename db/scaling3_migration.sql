DROP TABLE IF EXISTS `ai_status`;
CREATE TABLE `ai_status` (
  `server_type` VARCHAR(10) NOT NULL,
  `aiid` VARCHAR(50) NOT NULL,
  `training_status` VARCHAR(45) NOT NULL,
  `training_progress` FLOAT DEFAULT 0.0,
  `training_error` FLOAT DEFAULT 10000.0,  
  `server_endpoint` VARCHAR(256) NOT NULL,
  `queue_action` VARCHAR(50),
  `queue_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  PRIMARY KEY (`server_type`, `aiid`),    
  KEY `fk_aiid` (`aiid`),
  CONSTRAINT `fk_aiid` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `controller_state`;
CREATE TABLE `controller_state` (
  `server_type` VARCHAR(10) NOT NULL,
  `verified_server_count` INT,
  `training_capacity` INT,
  `training_slots_available` INT,
  `chat_capacity` INT,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  PRIMARY KEY (`server_type`)  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`ai_status` TO 'aiReader'@'127.0.0.1';
GRANT SELECT, UPDATE, DELETE ON `hutoma`.`ai_status` TO 'aiDeleter'@'127.0.0.1';
GRANT SELECT, INSERT, UPDATE ON `hutoma`.`controller_state` TO 'aiReader'@'127.0.0.1';

DROP PROCEDURE IF EXISTS `updateControllerState`;
DELIMITER $$
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
  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `updateAiStatus`;
DELIMITER $$
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
  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `deleteAIStatus`;
DELIMITER $$
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    DELETE FROM `ai_status` 
		WHERE `ai_status`.`server_type`=`in_server_type`
        AND `ai_status`.`aiid` = `in_aiid`;
  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `queueTakeNext`;
DELIMITER $$
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


  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `queueUpdate`;
DELIMITER $$
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

  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `queueCountSlots`;
DELIMITER $$
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueCountSlots`(
  IN `in_server_type` VARCHAR(10),
  IN `in_training_status` VARCHAR(50))
BEGIN

DECLARE v_cutoff DATETIME;
SET v_cutoff = DATE_SUB(NOW(), INTERVAL 5 MINUTE);

SELECT `ai_status`.`server_endpoint`,
 sum(case when `ai_status`.`update_time` > v_cutoff then 1 else 0 end) training,
 sum(case when `ai_status`.`update_time` <= v_cutoff then 1 else 0 end) lapsed
FROM `ai_status`
WHERE `server_type` = `in_server_type`
AND `ai_status`.`queue_time` IS NULL
AND `ai_status`.`training_status` = `in_training_status`
GROUP BY `ai_status`.`server_endpoint`;

  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `getAIsStatus`;
DELIMITER $$
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
  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `getAIsServerStatus`;
DELIMITER $$
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
    WHERE `ai`.`deleted` = 0;
  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `getAIQueueStatus`;
DELIMITER $$
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
  END$$
DELIMITER ;


DROP PROCEDURE `addAi`;
DELIMITER $$
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
                      ui_ai_language, ui_ai_timezone, ui_ai_confidence, ui_ai_personality, ui_ai_voice)
      VALUES (param_aiid, param_ai_name, param_ai_description, param_dev_id, param_is_private,
                          param_client_token,
                          param_ui_ai_language,
                          param_ui_ai_timezone, param_ui_ai_confidence, param_ui_ai_personality, param_ui_ai_voice);
      SET var_named_aiid = `param_aiid`;
    END IF;

    SELECT var_named_aiid AS aiid;

  END$$
DELIMITER ;

DROP PROCEDURE `getAi`;
DELIMITER $$
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
      `ui_ai_language`,
      `ui_ai_timezone`,
      `ui_ai_confidence`,
      `ui_ai_personality`,
      `ui_ai_voice`,
      (SELECT COUNT(`ai_training`.`aiid`)
       FROM `ai_training`
       WHERE `ai_training`.`aiid`=`in_aiid`)
        AS `has_training_file`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`aiid`=`in_aiid`
          AND `deleted`=0;

  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `getAiDeepLearningError`;
DROP PROCEDURE IF EXISTS `getAiDeepLearningStatus`;

DROP PROCEDURE `getAIs`;
DELIMITER $$
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
    `ai`.`ui_ai_language`,
    `ai`.`ui_ai_timezone`,
    `ai`.`ui_ai_confidence`,
    `ai`.`ui_ai_personality`,
    `ai`.`ui_ai_voice`,
    (SELECT COUNT(`ai_training`.`aiid`)
     FROM `ai_training`
     WHERE `ai_training`.`aiid`=`ai`.`aiid`)
      AS `has_training_file`,
    `botStore`.`publishing_state`
  FROM `ai` LEFT OUTER JOIN `botStore` on `botStore`.`aiid` = `ai`.`aiid`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `ai`.`deleted`=0;

  END$$
DELIMITER ;


DROP PROCEDURE `getAiStatus`;
DELIMITER $$
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
  END$$
DELIMITER ;

DROP PROCEDURE `getAiStatusAll`;
DELIMITER $$
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
  END$$
DELIMITER ;

DROP PROCEDURE `getAiStatusForUpdate`;
DELIMITER $$
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
  END$$
DELIMITER ;

DROP PROCEDURE `deleteAI`;
DELIMITER $$
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAi`(
  IN `in_dev_id` VARCHAR(50),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `deleted` = 1
		WHERE `dev_id`=`in_dev_id` AND `aiid`=`in_aiid`;
  END$$
DELIMITER ;

DROP PROCEDURE `deleteAllAIs`;
DELIMITER $$
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAllAIs`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
    UPDATE `ai` SET `deleted` = 1 WHERE `dev_id`=param_devid;
  END$$
DELIMITER ;

ALTER TABLE `hutoma`.`ai` 
DROP COLUMN `backend_status`;