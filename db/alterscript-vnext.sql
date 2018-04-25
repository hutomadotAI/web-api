/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/


USE `hutoma`;

ALTER TABLE `intent` ADD COLUMN `last_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `topic_out`;

ALTER TABLE `users`
  DROP COLUMN `username`,
  DROP COLUMN `email`,
  DROP COLUMN `password`,
  DROP COLUMN `password_salt`,
  DROP COLUMN `first_name`,
  DROP COLUMN `attempt`,
  DROP COLUMN `last_name`;


DROP PROCEDURE `addUpdateIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250),
  IN in_new_name varchar(250),
  IN in_topic_in varchar(250),
  IN in_topic_out varchar(250)
)
BEGIN
    INSERT INTO `intent` (`aiid`, `name`, `topic_in`, `topic_out`)
      SELECT `aiid`, `in_name`, `in_topic_in`, `in_topic_out`
      FROM ai
      WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`
    ON DUPLICATE KEY UPDATE `topic_in`=`in_topic_in`, `topic_out`=`in_topic_out`, `name`=`in_new_name`,
      `last_updated` = CURRENT_TIMESTAMP;
  END ;;
DELIMITER ;


DROP PROCEDURE `getIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntent`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id`, `name`, `topic_in`, `topic_out`, `last_updated`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
		AND `intent`.`aiid` = `in_aiid`;
END;;
DELIMITER ;


DROP PROCEDURE `addUser`;

DELIMITER ;;
CREATE DEFINER=`userTableWriter`@`127.0.0.1` PROCEDURE `addUser`(
  IN `param_dev_token` VARCHAR(250),
  IN `param_plan_id` INT,
  IN `param_dev_id` VARCHAR(50))
    MODIFIES SQL DATA
BEGIN
    INSERT INTO `users`(`dev_token`, `plan_id`, `dev_id`)
    VALUES (param_dev_token, param_plan_id, param_dev_id);
  END ;;
DELIMITER ;