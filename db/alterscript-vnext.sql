/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;


ALTER TABLE botStore ADD COLUMN `publishing_type` tinyint(1) NOT NULL DEFAULT 1 AFTER `publishing_state`;
CREATE INDEX `idx_botStore_publishing_state_publishing_type` on botStore (`publishing_state`, `publishing_type`);

DROP PROCEDURE IF EXISTS `publishBot`;
DELIMITER ;;
CREATE DEFINER=`botStoreWriter`@`127.0.0.1` PROCEDURE `publishBot`(
  IN `param_devId` VARCHAR(50),
  IN `param_aiid` VARCHAR(50),
  IN `param_name` VARCHAR(50),
  IN `param_description` VARCHAR(1024),
  IN `param_longDescription` TEXT,
  IN `param_alertMessage` VARCHAR(150),
  IN `param_badge` VARCHAR(20),
  IN `param_price` DECIMAL,
  IN `param_sample` TEXT,
  IN `param_lastUpdate` DATETIME,
  IN `param_category` VARCHAR(50),
  IN `param_licenseType` VARCHAR(50),
  IN `param_privacyPolicy` TEXT,
  IN `param_classification` VARCHAR(50),
  IN `param_version` VARCHAR(25),
  IN `param_videoLink` VARCHAR(1800),
  IN `param_publishingState` TINYINT(1),
  IN `param_publishingType` TINYINT(1)
)
    NO SQL
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
      ROLLBACK;
      SELECT -1;
    END;

    INSERT INTO botStore
    (dev_id, aiid, name, description, long_description, alert_message, badge, price, sample, last_update, category,
     privacy_policy, classification, version, video_link, license_type, publishing_state, publishing_type)
    VALUES (param_devId, param_aiid, param_name, param_description, param_longDescription, param_alertMessage,
                         param_badge, param_price, param_sample, param_lastUpdate, param_category, param_privacyPolicy, param_classification,
            param_version, param_videoLink, param_licenseType, param_publishingState, param_publishingType);

    SELECT LAST_INSERT_ID();
  END ;;
DELIMITER ;