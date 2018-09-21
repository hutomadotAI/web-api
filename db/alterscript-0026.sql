USE hutoma;

DROP PROCEDURE IF EXISTS `getAIsServerStatus`;

DELIMITER ;;
CREATE DEFINER=`aiReader`@`127.0.0.1` PROCEDURE `getAIsServerStatus`(
  IN `in_server_type` VARCHAR(10),
  IN `in_server_language` VARCHAR(3),
  IN `in_server_version` VARCHAR(10))
    READS SQL DATA
BEGIN
    SELECT 
        `ai_status`.`aiid`,
        `ai_status`.`server_language`,
        `ai_status`.`server_version`,
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
    WHERE `ai`.`deleted` = 0
        AND `ai_status`.`server_type` = `in_server_type`
        AND `ai_status`.`server_language` = `in_server_language`
        AND `ai_status`.`server_version` = `in_server_version`;
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
      `ai_status`.`server_language`,
      `ai_status`.`server_version`,
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