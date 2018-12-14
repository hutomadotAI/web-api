USE `hutoma`;

LOCK TABLES `entity_value` WRITE;

ALTER TABLE `hutoma`.`entity_value` 
CHANGE `value` `value` VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;

UNLOCK TABLES;