/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;

ALTER TABLE `chatState` ADD COLUMN `history` VARCHAR(1024) DEFAULT NULL AFTER `topic`;
ALTER TABLE `chatState` ADD COLUMN `confidence_threshold` DOUBLE DEFAULT NULL AFTER `entity_values`;


DROP PROCEDURE IF EXISTS `setChatState`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_timestamp` TIMESTAMP,
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE)
BEGIN
    INSERT INTO chatState (dev_id, chat_id, timestamp, topic, history, locked_aiid, entity_values, confidence_threshold)
    VALUES(param_devId, param_chatId, param_timestamp, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold)
    ON DUPLICATE KEY UPDATE timestamp = param_timestamp, topic = param_topic, history = param_history,
      locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold;
  END ;;
DELIMITER ;
