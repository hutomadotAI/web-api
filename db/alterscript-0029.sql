USE hutoma;

DROP PROCEDURE IF EXISTS `getEntities`;

DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntities`(
  IN in_dev_id VARCHAR(50), IN includeHidden BOOLEAN)
BEGIN
    SELECT `name`, `isSystem` FROM `entity` WHERE
    (`entity`.`dev_id`=`in_dev_id` OR `entity`.`isSystem`=1) AND (`entity`.`hid$
END ;;
DELIMITER ;

