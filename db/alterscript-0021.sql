USE hutoma;


ALTER TABLE `ai_status`
  ADD COLUMN `server_language` VARCHAR(3) NOT NULL DEFAULT "EN" AFTER `server_type`,
  ADD COLUMN `server_version` VARCHAR(10) NOT NULL DEFAULT "default" AFTER `server_language`,
  DROP PRIMARY KEY, ADD PRIMARY KEY(`server_type`,`server_language`,`server_version`,`aiid`);

ALTER TABLE `controller_state`
  ADD COLUMN `server_language` VARCHAR(3) NOT NULL DEFAULT `EN` AFTER `server_type`,
  ADD COLUMN `server_version` VARCHAR(10) NOT NULL DEFAULT "default" AFTER `server_language`.
  DROP PRIMARY KEY, ADD PRIMARY KEY(`server_type`,`server_language`,`server_version`);


DROP PROCEDURE IF EXISTS `queueUpdate`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueUpdate`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
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

UPDATE `ai_status`
SET
    `queue_time`=v_queue_time,
    `queue_action`=`in_queue_action`,
    `update_time`=now()
WHERE `server_type` = `in_server_type`
    AND `server_language` = `in_server_language`
    AND `server_version` = `in_server_version`
    AND `aiid` = `in_aiid`;

  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `updateAiStatus`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_training_status` VARCHAR(45),
  IN `in_server_endpoint` VARCHAR(256),
  IN `in_training_progress` FLOAT,
  IN `in_training_error` FLOAT)
BEGIN

INSERT INTO `ai_status`
( `server_type`,
  `server_language`,
  `server_version`,
  `aiid`,
  `training_status`,
  `training_progress`,
  `training_error`,
  `server_endpoint`)
VALUES
( `in_server_type`,
  `in_server_language`,
  `in_server_version`,
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
END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `queueCountSlots`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueCountSlots`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
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
  AND `server_language` = `in_server_language`
  AND `server_version` = `in_server_version`
  AND `ai_status`.`queue_time` IS NULL
  AND `ai_status`.`training_status` = `in_training_status`
GROUP BY `ai_status`.`server_endpoint`;

END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `queueTakeNext`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueTakeNext`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10))
BEGIN

DECLARE v_aiid VARCHAR(50);
SELECT `ai_status`.`aiid` INTO v_aiid
  FROM `ai_status`
WHERE `server_type` = `in_server_type`
  AND `server_language` = `in_server_language`
  AND `server_version` = `in_server_version`
  AND `queue_time`<now()
ORDER BY `queue_time` ASC
LIMIT 1 FOR UPDATE;

IF NOT v_aiid IS NULL THEN
    UPDATE `ai_status` SET `queue_time`=NULL
    WHERE `aiid` = v_aiid
        AND `in_server_type`=`server_type`;
END IF;

SELECT *
FROM `ai_status`
WHERE v_aiid IS NOT NULL
  AND `aiid`=v_aiid
  AND `in_server_type`=`server_type`
  AND `server_language` = `in_server_language`
  AND `server_version` = `in_server_version`;

END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getAIQueueStatus`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIQueueStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
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
    FROM `ai_status` JOIN `ai` USING (`aiid`)
    WHERE `ai_status`.`aiid` = `in_aiid`
      AND `ai_status`.`server_type` = `in_server_type`
      AND `ai_status`.`server_language` = `in_server_language`
      AND `ai_status`.`server_version` = `in_server_version`;
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getInterruptedTrainingList`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getInterruptedTrainingList`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
  IN `in_training_status` VARCHAR(50),
  IN `in_cutoff_seconds` INT)
BEGIN

DECLARE v_cutoff DATETIME;
SET v_cutoff = DATE_SUB(NOW(), INTERVAL `in_cutoff_seconds` SECOND);

SELECT `ai_status`.*
FROM `ai_status`
WHERE `ai_status`.`server_type` = `in_server_type`
  AND `ai_status`.`server_language` = `in_server_language`
  AND `ai_status`.`server_version` = `in_server_version`
  AND `ai_status`.`queue_time` IS NULL
  AND `ai_status`.`training_status` = `in_training_status`
  AND `update_time`<=v_cutoff
ORDER BY `update_time` ASC
FOR UPDATE;

END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `queueRecover`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `queueRecover`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
  IN `in_aiid` VARCHAR(50),
  IN `in_queue_action` VARCHAR(50),
  IN `in_training_status` VARCHAR(45)
  )
BEGIN

UPDATE `ai_status`
SET
  `training_status` = `in_training_status`,
  `queue_action`=`in_queue_action`,
  `update_time`=now(),
  `queue_time`=now()
WHERE `server_type` = `in_server_type`
  AND `server_language` = `in_server_language`
  AND `server_version` = `in_server_version`
  AND `aiid` = `in_aiid`;

  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `deleteAiStatus`;
DELIMITER ;;
CREATE DEFINER=`aiDeleter`@`127.0.0.1` PROCEDURE `deleteAiStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
  IN `in_aiid` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    DELETE FROM `ai_status`
    WHERE `ai_status`.`server_type`=`in_server_type`
      AND `ai_status`.`server_language` = `in_server_language`
      AND `ai_status`.`server_version` = `in_server_version`
      AND `ai_status`.`aiid` = `in_aiid`;
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `updateControllerState`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `updateControllerState`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10),
  IN `in_verified_server_count` INT,
  IN `in_training_capacity` INT,
  IN `in_training_slots_available` INT,
  IN `in_chat_capacity` INT)
BEGIN
INSERT INTO `controller_state`
  (`server_type`,
  `server_language`,
  `server_version`,
  `verified_server_count`,
  `training_capacity`,
  `training_slots_available`,
  `chat_capacity`,
  `update_time`)
VALUES
  (`in_server_type`,
  `in_server_language`,
  `in_server_version`,
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
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getAiStatus`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAiStatus`(
  IN `param_aiid` VARCHAR(50),
  IN `param_devid` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
      `ai_status`.`server_type`,
      `ai_status`.`server_language`,
      `ai_status`.`server_version`,
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
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getAIsStatus`;
DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIsStatus`(
  IN `in_dev_id` VARCHAR(50))
    READS SQL DATA
BEGIN
    SELECT
      `ai_status`.`server_type`,
      `ai_status`.`server_language`,
      `ai_status`.`server_version`,
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
  END;;
DELIMITER ;