ALTER TABLE `hutoma`.`entity` 
ADD COLUMN `aiid` VARCHAR(45) '' AFTER `value_type`;


ALTER TABLE `hutoma`.`entity` 
DROP FOREIGN KEY `entity_ibfk_1`;
ALTER TABLE `hutoma`.`entity` 
DROP INDEX `dev_id` ;
;

ALTER TABLE `hutoma`.`entity` 
ADD UNIQUE INDEX `dev_id` (`dev_id` ASC, `name` ASC, `aiid` ASC);
;


DROP PROCEDURE `hutoma`.`getEntities`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntities`(
  IN in_dev_id VARCHAR(50), IN in_aiid VARCHAR(50))
BEGIN
    SELECT `name`, `isSystem`, `value_type` 
    FROM `entity` 
    WHERE (`entity`.`dev_id`=`in_dev_id` AND `entity`.`aiid`=`in_aiid` OR `entity`.`isSystem`=1) AND (`entity`.`hidden`<>1);
END


DROP PROCEDURE `hutoma`.`getEntityDetails`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityDetails`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT * FROM `entity`
    WHERE `entity`.`name`=`in_name`
          AND (`entity`.`dev_id`=`in_dev_id` AND `entity`.`aiid`=`in_aiid` OR `entity`.`isSystem`=1);
  END

DROP PROCEDURE `hutoma`.`getEntityValues`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityValues`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT `entity_value`.`value` FROM `entity`,`entity_value`
    WHERE `entity`.`dev_id`=`in_dev_id`
          AND `entity`.`name`=`in_name`
          AND `entity`.`id`=`entity_value`.`entity_id`
          and `entity`.`aiid`=`in_aiid`;
  END

DROP PROCEDURE `hutoma`.`getEntityIdForDev`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityIdForDev`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name` AND `in_aiid`=`aiid`;
END

DROP PROCEDURE `hutoma`.`getEntityValuesCountForDevExcludingEntity`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `getEntityValuesCountForDevExcludingEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_aiid VARCHAR(50))
BEGIN
    SELECT COUNT(*) as COUNT
  FROM `entity` e INNER JOIN `entity_value` ev ON ev.`entity_id`=e.`id` 
  WHERE e.`dev_id`=in_dev_id AND e.`name`<>in_name AND e.`aiid`=in_aiid;
END


DROP PROCEDURE `hutoma`.`deleteEntityValue`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `deleteEntityValue`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_value VARCHAR(250),
  IN in_aiid VARCHAR(250))
BEGIN
    DELETE FROM `entity_value` WHERE `in_value`=`value` AND `entity_id`=
                                                            (SELECT `id` FROM `entity` WHERE `in_dev_id`=`dev_id` AND `in_name`=`name` AND `in_aiid`=`aiid`);
  END

DROP PROCEDURE `hutoma`.`addUpdateEntity`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `addUpdateEntity`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_new_name VARCHAR(250),
  IN in_value_type VARCHAR(10),
  IN in_aiid VARCHAR(50))
BEGIN
    INSERT INTO `entity` (`dev_id`, `name`, `value_type`, `aiid`) 
    VALUES (`in_dev_id`, `in_name`, `in_value_type`, `in_aiid`)
    ON DUPLICATE KEY UPDATE `name`=`in_new_name`, `value_type`=`in_value_type`, `aiid`=`in_aiid`;
  END

DROP PROCEDURE `hutoma`.`addEntityValue`;

CREATE DEFINER=`entityUser`@`127.0.0.1` PROCEDURE `addEntityValue`(
  IN in_dev_id VARCHAR(50),
  IN in_name VARCHAR(250),
  IN in_value VARCHAR(250),
  IN in_aiid VARCHAR(50))
BEGIN
    INSERT INTO `entity_value` (`entity_id`,`value`)
      SELECT `id`, `in_value` FROM `entity`
      WHERE `in_dev_id`=`dev_id` AND `in_name`=`name` AND `in_aiid`=`aiid`;
  END

