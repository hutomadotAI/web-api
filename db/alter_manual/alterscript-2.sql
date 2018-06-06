USE `hutoma`;

ALTER TABLE `chatState` 
  ADD COLUMN `base_aiid` VARCHAR(50) NULL DEFAULT NULL AFTER `chat_id`,
  ADD COLUMN `current_intents` MEDIUMTEXT NULL DEFAULT NULL AFTER `context`,
  ADD INDEX (`dev_id`,`base_aiid`);

DROP TABLE `memoryIntents`;
DROP PROCEDURE `deleteAllMemoryIntents`;
DROP PROCEDURE `deleteMemoryIntent`;
DROP PROCEDURE `getMemoryIntent`;
DROP PROCEDURE `getMemoryIntentsForChat`;
DROP PROCEDURE `updateMemoryIntent`;

DROP PROCEDURE `setChatState`;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `setChatState`(
  IN `param_devId` VARCHAR(50),
  IN `param_chatId` VARCHAR(50),
  IN `param_base_aiid` VARCHAR(50),
  IN `param_topic` VARCHAR(250),
  IN `param_history` VARCHAR(1024),
  IN `param_locked_aiid` VARCHAR(50),
  IN `param_entity_values` TEXT,
  IN `param_confidence_threshold` DOUBLE,
  IN `param_chat_target` TINYINT(1),
  IN `param_handover_reset` TIMESTAMP,
  IN `param_bad_answers_count` INT(11),
  IN `param_context` MEDIUMTEXT,
  IN `param_current_intents` MEDIUMTEXT)
BEGIN
  INSERT INTO chatState (dev_id, chat_id, base_aiid, topic, history, locked_aiid, entity_values, confidence_threshold, chat_target, 
    handover_reset, bad_answers_count, context, current_intents)
  VALUES(param_devId, param_chatId, param_base_aiid, param_topic, param_history, param_locked_aiid, param_entity_values, param_confidence_threshold, 
    param_chat_target, param_handover_reset, param_bad_answers_count, param_context, param_current_intents)
  ON DUPLICATE KEY UPDATE base_aiid = param_base_aiid, topic = param_topic, history = param_history,
    locked_aiid = param_locked_aiid, entity_values = param_entity_values, confidence_threshold = param_confidence_threshold, 
    chat_target = param_chat_target, handover_reset = param_handover_reset, bad_answers_count = param_bad_answers_count,
    context = param_context, current_intents = param_current_intents;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `resetChatStatesForAi`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50)
)
BEGIN
  UPDATE `chatState`
  SET 
    `context` = NULL,
    `current_intents` = NULL,
    `bad_answers_count` = 0
  WHERE `dev_id`=param_devId AND `base_aiid`=param_aiid;
END ;;
DELIMITER ;
