USE hutoma;

ALTER TABLE `entity` 
	ADD COLUMN `value_type` VARCHAR(10) NOT NULL DEFAULT "LIST";

UPDATE `entity` SET `value_type`="SYS" WHERE isSystem=1;


DROP PROCEDURE IF EXISTS `addUpdateEntity`;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `addUpdateEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_new_name VARCHAR(250),
  IN in_value_type VARCHAR(10))
BEGIN
    INSERT INTO `entity` (`dev_id`, `name`, `value_type`) 
    VALUES (`in_dev_id`, `in_name`, `in_value_type`)
    ON DUPLICATE KEY UPDATE `name`=`in_new_name`, `value_type`=`in_value_type`;
  END;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `getEntities`;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntities`(
  IN in_dev_id VARCHAR(50))
BEGIN
    SELECT `name`, `isSystem`, `value_type` 
    FROM `entity` 
    WHERE (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1) AND (`entity`.`hidden`<>1);
END;;
DELIMITER ;
