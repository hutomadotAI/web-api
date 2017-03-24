DROP TABLE IF EXISTS `ai_status`;
CREATE TABLE `ai_status` (
  `server_type` VARCHAR(10) NOT NULL,
  `aiid` VARCHAR(50) NOT NULL,
  `training_status` VARCHAR(45) NOT NULL,
  `training_progress` FLOAT DEFAULT 0.0,
  `training_error` FLOAT DEFAULT 10000.0,  
  `server_endpoint` VARCHAR(256) NOT NULL,
  `queue_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  PRIMARY KEY (`server_type`, `aiid`),    
  KEY `fk_aiid` (`aiid`),
  CONSTRAINT `fk_aiid` FOREIGN KEY (`aiid`) REFERENCES `ai` (`aiid`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

GRANT SELECT, INSERT, UPDATE, DELETE ON `hutoma`.`ai_status` TO 'aiReader'@'127.0.0.1';
GRANT SELECT, UPDATE, DELETE ON `hutoma`.`ai_status` TO 'aiDeleter'@'127.0.0.1';

DROP PROCEDURE IF EXISTS `updateAiStatus`;
DELIMITER $$
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_training_status` VARCHAR(45),
  IN `in_server_endpoint` VARCHAR(256),
  IN `in_training_progress` FLOAT,
  IN `in_training_error` FLOAT,
  IN `flag_queue` TINYINT,
  IN `in_queue_time` DATETIME)
BEGIN

DECLARE v_queue_time DATETIME;
IF (`flag_queue` = 0) THEN
	SET v_queue_time = NULL;
ELSE
	IF (`in_queue_time` IS NULL) THEN
		SET v_queue_time = now();
	ELSE 
		SET v_queue_time = `in_queue_time`;
	END IF;
END IF;

INSERT INTO `ai_status` 
( `server_type`,
  `aiid`,
  `training_status`,
  `training_progress`,
  `training_error`,
  `server_endpoint`,
  `queue_time`) 
VALUES 
( `in_server_type`,
  `in_aiid`,
  `in_training_status`,
  `in_training_progress`,
  `in_training_error`,
  `in_server_endpoint`,
  v_queue_time)
ON DUPLICATE KEY UPDATE 
	`training_status`=`in_training_status`,	
    `training_progress`=`in_training_progress`,	
    `training_error`=`in_training_error`,
	`server_endpoint`=`in_server_endpoint`,
    `update_time`=now(),
	`queue_time`=v_queue_time;
  END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `takeNextQueued`;
DELIMITER $$
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `takeNextQueued`(
  IN `in_server_type` VARCHAR(10),
  IN `in_training_status` VARCHAR(45))
BEGIN

DECLARE v_aiid VARCHAR(50);
DECLARE v_server_endpoint VARCHAR(256);
DECLARE v_queue_time DATETIME;

SELECT 
	`aiid`,
	`server_endpoint`,
	`queue_time`
INTO 
	v_aiid,
	v_server_endpoint,
	v_queue_time
FROM `ai_status` 
WHERE `server_type` = `in_server_type` 
AND `training_status`=`in_training_status`
ORDER BY `queue_time`
LIMIT 1 FOR UPDATE;

IF v_aiid THEN
    UPDATE `ai_status` 
    SET `queue_time`=NULL 
    WHERE `aiid` = v_aiid;
    SELECT 
		v_aiid as `aiid`,
		v_server_endpoint as `server_endpoint`,
		v_queue_time as `queue_time`;
END IF;

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
        `ai_status`.`training_error`
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
        `ai_status`.`server_endpoint`
    FROM `ai_status`
    JOIN `ai` USING (`aiid`)
    WHERE `ai`.`deleted` = 0;
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
       WHERE `ai_training`.`aiid`=`ai`.`aiid`)
        AS `has_training_file`
    FROM `ai`
    WHERE `ai`.`dev_id`=`in_dev_id`
          AND `deleted`=0;

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
        `ai_status`.`training_error`
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
        `ai_status`.`training_error`
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
        `ai_status`.`training_error`
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
	DELETE FROM `ai_status` WHERE `aiid` IN 
		(SELECT `aiid` FROM `ai` WHERE `aiid`=`in_aiid` AND `dev_id`=`in_dev_id` FOR UPDATE);
    UPDATE `ai` SET `deleted` = 1
		WHERE `dev_id`=`in_dev_id` AND `aiid`=`in_aiid`;
  END$$
DELIMITER ;

DROP PROCEDURE `deleteAllAIs`;
DELIMITER $$
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAllAIs`(IN `param_devid` varchar(50))
    MODIFIES SQL DATA
BEGIN
	DELETE FROM `ai_status` WHERE `aiid` IN 
		(SELECT `aiid` FROM `ai` WHERE `dev_id`=param_devid FOR UPDATE);
    UPDATE `ai` SET `deleted` = 1 WHERE `dev_id`=param_devid;
  END$$
DELIMITER ;

ALTER TABLE `hutoma`.`ai` 
DROP COLUMN `backend_status`;