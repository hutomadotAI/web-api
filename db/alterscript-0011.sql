USE `hutoma`;

ALTER TABLE `hutoma`.`intent_response` MODIFY COLUMN `response` VARCHAR(1024);
ALTER TABLE `hutoma`.`intent_user_says` MODIFY COLUMN `says` VARCHAR(1024);


DROP PROCEDURE `deleteIntentResponse`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntentResponse`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_response VARCHAR(1024))
BEGIN
    DELETE FROM `intent_response`
    WHERE `in_response`=`response` AND `intent_id`=
                                       (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                                                  (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
  END ;;
DELIMITER ;

DROP PROCEDURE `addIntentResponse`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addIntentResponse`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_response VARCHAR(1024))
BEGIN
    INSERT INTO `intent_response` (`intent_id`, `response`)
      SELECT `id`, `in_response` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                               (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
  END ;;
DELIMITER ;


DROP PROCEDURE `deleteIntentUserSays`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `deleteIntentUserSays`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_says VARCHAR(1024))
BEGIN
    DELETE FROM `intent_user_says`
    WHERE `in_says`=`says` AND `intent_id`=
                               (SELECT `id` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                                          (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`));
  END ;;
DELIMITER ;


DROP PROCEDURE `addIntentUserSays`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addIntentUserSays`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_says VARCHAR(1024))
BEGIN
    INSERT INTO `intent_user_says` (`intent_id`, `says`)
      SELECT `id`, `in_says` FROM `intent` WHERE `in_name`=`name` AND `in_aiid`=`aiid` AND `in_aiid` IN
                                                                                           (SELECT `aiid` FROM `ai` WHERE `in_dev_id`=`dev_id`);
  END ;;
DELIMITER ;