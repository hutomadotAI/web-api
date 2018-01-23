/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/


USE `hutoma`;

GRANT SELECT ON `hutoma`.`intent` TO 'entityUser'@'127.0.0.1';
GRANT SELECT ON `hutoma`.`ai` TO 'entityUser'@'127.0.0.1';

DROP PROCEDURE `deleteEntity`;
DELIMITER ;;
CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_entity_id int(11))
BEGIN
    DELETE FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_entity_id`=`id`
      AND NOT EXISTS (
     SELECT NULL FROM `intent_variable` iv INNER JOIN `intent` ON `intent`.`id`=iv.`intent_id` INNER JOIN `ai` ON `ai`.`aiid`=`intent`.`aiid` 
     WHERE iv.`entity_id`=`in_entity_id` AND `ai`.`deleted`=0);
END;;
DELIMITER ;