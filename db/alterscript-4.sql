USE `hutoma`;

ALTER TABLE `intent`
  ADD COLUMN `context_in` MEDIUMTEXT NULL DEFAULT NULL AFTER `last_updated`,
  ADD COLUMN `context_out` MEDIUMTEXT NULL DEFAULT NULL AFTER `context_in`;


DROP PROCEDURE `addUpdateIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `addUpdateIntent`(
  IN in_dev_id VARCHAR(50),
  IN in_aiid VARCHAR(50),
  IN in_name varchar(250),
  IN in_new_name varchar(250),
  IN in_topic_in varchar(250),
  IN in_topic_out varchar(250),
  IN in_context_in MEDIUMTEXT,
  IN in_context_out MEDIUMTEXT
)
BEGIN
    INSERT INTO `intent` (`aiid`, `name`, `topic_in`, `topic_out`, `context_in`, `context_out`)
      SELECT `aiid`, `in_name`, `in_topic_in`, `in_topic_out`, `in_context_in`, `in_context_out`
      FROM ai
      WHERE `in_dev_id`=`dev_id` AND `in_aiid`=`aiid`
    ON DUPLICATE KEY UPDATE `topic_in`=`in_topic_in`, `topic_out`=`in_topic_out`, `name`=`in_new_name`,
        `context_in` = `in_context_in`, `context_out` = `in_context_out`;
  END ;;
DELIMITER ;


DROP PROCEDURE `getIntent`;

DELIMITER ;;
CREATE DEFINER=`intentUser`@`127.0.0.1` PROCEDURE `getIntent`(
  IN in_aiid VARCHAR(50),
  IN in_name VARCHAR(250))
BEGIN
    SELECT `id`, `name`, `topic_in`, `topic_out`, `last_updated`, `context_in`, `context_out`
    FROM `intent`
    WHERE `intent`.`name`=`in_name`
    AND `intent`.`aiid` = `in_aiid`;
END;;