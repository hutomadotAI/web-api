USE `hutoma`;

DROP TABLE IF EXISTS `invite_code_uses`;
DROP TABLE IF EXISTS `invite_codes`;
DROP TABLE IF EXISTS `resetTokens`;
DROP TABLE IF EXISTS `userAIDomains`;
DROP TABLE IF EXISTS `memoryIntent`;

DROP PROCEDURE `redeemInviteCode`;
DROP PROCEDURE `existsInviteCode`;
DROP PROCEDURE `deletePasswordResetToken`;
DROP PROCEDURE `getUserIdForResetToken`;
DROP PROCEDURE `insertResetToken`;
DROP PROCEDURE `isPasswordResetTokenValid`;
DROP PROCEDURE `insertUserActiveDomain`;
DROP PROCEDURE `getDomainsAndUserActiveDomains`;
DROP PROCEDURE `addUserComplete`;
DROP PROCEDURE `getUser`;
DROP PROCEDURE `getUserById`;
DROP PROCEDURE `getUserDetails`;
DROP PROCEDURE `getUserId`;
DROP PROCEDURE `getUserIdFromUsername`;
DROP PROCEDURE `updateUserLoginAttempts`;
DROP PROCEDURE `updateUserPassword`;
DROP PROCEDURE `userExists`;