/*
The purpose of this script file is to include all the db alterations
required for the next deployment.
*/

USE `hutoma`;


ALTER TABLE botStore ADD COLUMN `publishing_type` tinyint(1) NOT NULL DEFAULT 1 AFTER `publishing_state`;
CREATE INDEX `idx_botStore_publishing_state_publishing_type` on botStore (`publishing_state`, `publishing_type`);

