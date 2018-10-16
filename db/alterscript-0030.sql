USE hutoma;

DROP PROCEDURE IF EXISTS `updateAi`;
DELIMITER ;;
CREATE DEFINER=`aiWriter`@`127.0.0.1` PROCEDURE `updateAi`(
  IN `param_aiid` VARCHAR(50),
  IN `param_ai_description` VARCHAR(250),
  IN `param_dev_id` VARCHAR(50),
  IN `param_is_private` TINYINT(1),
  IN `param_ui_ai_language` VARCHAR(10),
  IN `param_ui_ai_timezone` VARCHAR(50),
  IN `param_ui_ai_confidence` DOUBLE,
  IN `param_ui_ai_personality` TINYINT(4),
  IN `param_ui_ai_voice` VARCHAR(50),
  IN `param_default_chat_responses` TEXT,
  IN `param_error_threshold_handover` INT(11),
  IN `param_handover_reset_timeout` INT(11),
  IN `param_handover_message` VARCHAR(2048),
  IN `param_engine_version` VARCHAR(10))
    MODIFIES SQL DATA
BEGIN
    update ai
    set
      ai_description = param_ai_description,
      is_private = param_is_private,
      ui_ai_language = param_ui_ai_language,
      ui_ai_timezone = param_ui_ai_timezone,
      ui_ai_confidence = param_ui_ai_confidence,
      ui_ai_personality = param_ui_ai_personality,
      ui_ai_voice = param_ui_ai_voice,
      default_chat_responses = param_default_chat_responses,
      handover_message = param_handover_message,
      handover_reset_timeout = param_handover_reset_timeout,
      error_threshold_handover = param_error_threshold_handover,
      engine_version = param_engine_version
    where aiid = param_aiid AND dev_id = param_dev_id;
  END;;
DELIMITER ;