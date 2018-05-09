/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/


USE `hutoma`;

ALTER TABLE `chatState` ADD COLUMN `context` MEDIUMTEXT NULL DEFAULT NULL AFTER `bad_answers_count`;


DROP PROCEDURE `setChatState`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE,
  IN `param_chat_target` TINYINT(1),
  IN `param_handover_reset` TIMESTAMP,
  IN `param_bad_answers_count` INT(11),
  IN `param_context` MEDIUMTEXT)
BEGIN
    INSERT INTO chatState (dev_id, chat_id, topic, history, locked_aiid, entity_values, confidence_threshold, chat_target, 
      handover_reset, bad_answers_count, context)
    VALUES(param_devId, param_chatId, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold, 
      param_chat_target, param_handover_reset, param_bad_answers_count, param_context)
    ON DUPLICATE KEY UPDATE topic = param_topic, history = param_history,
      locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold, 
      chat_target = param_chat_target, handover_reset = param_handover_reset, bad_answers_count = param_bad_answers_count,
      context = param_context;
  END ;;
DELIMITER ;
