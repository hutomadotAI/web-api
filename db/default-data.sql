
USE `hutoma`;
--
-- Dumping data for table `devplan`
--

LOCK TABLES `devplan` WRITE;
/*!40000 ALTER TABLE `devplan` DISABLE KEYS */;
INSERT INTO `devplan` VALUES (1,1,1,1,30,1),
(2,2,2,2,380,2),
(3,3,3,3,1440,3),
(4,4,4,4,24000,4);
/*!40000 ALTER TABLE `devplan` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `integrations` WRITE;
/*!40000 ALTER TABLE `integrations` DISABLE KEYS */;
INSERT INTO `integrations` VALUES 
	(2,'Facebook Messenger','An easier way to message','messenger.png',0),
	(4,'Slack','A messaging app for teams','slack.png',0),
	(5,'Telegram','A new era of messaging','telegram.png',0),
	(6,'Twilio','Programmable SMS','twilio.png',0),
	(7,'Webhook','A way to plug a web service into your agent','webhook.png',0),
	(8,'WeChat','A text and voice messaging communication','wechat.png',0);
/*!40000 ALTER TABLE `integrations` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('bot-support@hutoma.com','bot-support@hutoma.com','6194f28feb9934d7496a6f522d108fdebbf6ac9161123ef71f6d75b61b8ef0c7','PAgskb3GGJ1bss2N0MiS','Hutoma','2017-01-31 18:15:14','0','eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkRRDkyQTE8Mk3VSjRCNdkyTLNN2kVMskXRNjA0MzE_NUQyMzU6VaAAAAAP__.oeOfH1ninCYCkjVOPC5IF4eZPBNpZCuI3_hS5HWP9CA',1,'d14b441b-e2a2-4b9f-be9b-4301647e1265','eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8c4-nq5-If5-PpFKOkrFpUlA8RRDkyQTE8Mk3VSjRCNdkyTLNN2kVMskXRNjA0MzE_NUQyMzU6VaAAAAAP__.OWYIKxrqOCMSfVjmyWisK-jj7DPVnFaWBn9J5qs1kQQ','',1,1,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `developerInfo` WRITE;
/*!40000 ALTER TABLE `developerInfo` DISABLE KEYS */;
INSERT INTO developerInfo VALUES('d14b441b-e2a2-4b9f-be9b-4301647e1265', 'Hutoma', 'Hu:toma','support@hutoma.com','Davidson House, The Forbury,','RG1 3EU','Reading','United Kingdom','http://www.hutoma.com');
/*!40000 ALTER TABLE `developerInfo` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `ai` WRITE;
/*!40000 ALTER TABLE `ai` DISABLE KEYS */;
INSERT INTO `ai` VALUES (1,'e1bb8226-e8ce-467a-8305-bc2fcb89dd7f','AIML Chit-chat','','2017-01-31 18:10:17','d14b441b-e2a2-4b9f-be9b-4301647e1265',0,'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNocijEKwzAMAP-iuQLZUhypW2kyGEwCpUunEtnpB0qn0L_XdDqOuwNua5nh_MfzWvK83NelPOAEl5ynHppH8tEZnbShmBFuMURsQ5K98qbCr36_P95nNe4eAlKyhCLGqFUTEqt5lXF3GeD7AwAA__8.vLFd5SSUH37G5aFq7byG5ZBDZiX5XHs2RJLS_MfWLhg','en-US','Europe/London',0.4000000059604645,0,0,0);
INSERT INTO `ai` VALUES (2,'491091b1-5458-4d87-b7e9-0ccf3002970c','AIML Concept-net','','2017-01-31 18:10:17','d14b441b-e2a2-4b9f-be9b-4301647e1265',0,'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNocijEKwzAMAP-iuQLZUhypW2kyGEwCpUunEtnpB0qn0L_XdDqOuwNua5nh_MfzWvK83NelPOAEl5ynHppH8tEZnbShmBFuMURsQ5K98qbCr36_P95nNe4eAlKyhCLGqFUTEqt5lXF3GeD7AwAA__8.vLFd5SSUH37G5aFq7byG5ZBDZiX5XHs2RJLS_MfWLhg','en-US','Europe/London',0.4000000059604645,0,0,0);
INSERT INTO `ai` VALUES (3,'9a7a3376-e872-463e-aaa5-9f7e0e8c3380','AIML Jokes','','2017-01-31 18:10:17','d14b441b-e2a2-4b9f-be9b-4301647e1265',0,'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNocijEKwzAMAP-iuQLZUhypW2kyGEwCpUunEtnpB0qn0L_XdDqOuwNua5nh_MfzWvK83NelPOAEl5ynHppH8tEZnbShmBFuMURsQ5K98qbCr36_P95nNe4eAlKyhCLGqFUTEqt5lXF3GeD7AwAA__8.vLFd5SSUH37G5aFq7byG5ZBDZiX5XHs2RJLS_MfWLhg','en-US','Europe/London',0.4000000059604645,0,0,0);
INSERT INTO `ai` VALUES (4,'873e61b9-9b75-449b-9532-821fcea6f864','AIML Mika','','2017-01-31 18:10:17','d14b441b-e2a2-4b9f-be9b-4301647e1265',0,'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNocijEKwzAMAP-iuQLZUhypW2kyGEwCpUunEtnpB0qn0L_XdDqOuwNua5nh_MfzWvK83NelPOAEl5ynHppH8tEZnbShmBFuMURsQ5K98qbCr36_P95nNe4eAlKyhCLGqFUTEqt5lXF3GeD7AwAA__8.vLFd5SSUH37G5aFq7byG5ZBDZiX5XHs2RJLS_MfWLhg','en-US','Europe/London',0.4000000059604645,0,0,0);
/*!40000 ALTER TABLE `ai` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `botStore` WRITE;
/*!40000 ALTER TABLE `botStore` DISABLE KEYS */;
INSERT INTO `botStore` (`id`,`dev_id`,`aiid`,`name`,`description`,`long_description`,`alert_message`,`badge`,`license_type`,`price`,`sample`,`last_update`,`category`,`privacy_policy`,`classification`,`version`,`video_link`,`publishing_state`,`botIcon`)
VALUES (1,'d14b441b-e2a2-4b9f-be9b-4301647e1265','e1bb8226-e8ce-467a-8305-bc2fcb89dd7f','Chit-Chat','Free Chit-Chat bot','Just include this bot in your AI to get instant chit-chat','','','Free',0,'Hi
What''s your name?
What is MY name?','2017-01-31 00:00:00','Other','https://www.hutoma.com/privacy.pdf','EVERYONE','1.0','',2,'_chat.png');
INSERT INTO `botStore` (`id`,`dev_id`,`aiid`,`name`,`description`,`long_description`,`alert_message`,`badge`,`license_type`,`price`,`sample`,`last_update`,`category`,`privacy_policy`,`classification`,`version`,`video_link`,`publishing_state`,`botIcon`)
VALUES (2,'d14b441b-e2a2-4b9f-be9b-4301647e1265','491091b1-5458-4d87-b7e9-0ccf3002970c','Knowledge Bot','Free general knowledge bot','Include this bot and your AI will be able to answer simple general knowledge questions','','','Free',0,'What can i do with a hammer
Where can i find a chair
What has legs','2017-01-31 00:00:00','Other','https://www.hutoma.com/privacy.pdf','EVERYONE','1.0','',2,'_generalknowledge.png');
INSERT INTO `botStore` (`id`,`dev_id`,`aiid`,`name`,`description`,`long_description`,`alert_message`,`badge`,`license_type`,`price`,`sample`,`last_update`,`category`,`privacy_policy`,`classification`,`version`,`video_link`,`publishing_state`,`botIcon`)
VALUES (3,'d14b441b-e2a2-4b9f-be9b-4301647e1265','9a7a3376-e872-463e-aaa5-9f7e0e8c3380','Jokes','Free jokes dispenser','Ask for a joke','','','Free',0,'Tell me a joke
Joke','2017-01-31 00:00:00','Other','https://www.hutoma.com/privacy.pdf','EVERYONE','1.0','',2,'_jokes.png');
INSERT INTO `botStore` (`id`,`dev_id`,`aiid`,`name`,`description`,`long_description`,`alert_message`,`badge`,`license_type`,`price`,`sample`,`last_update`,`category`,`privacy_policy`,`classification`,`version`,`video_link`,`publishing_state`,`botIcon`)
VALUES (4,'d14b441b-e2a2-4b9f-be9b-4301647e1265','873e61b9-9b75-449b-9532-821fcea6f864','Mika','Multi-purpose Intuitive Knowledge Assistant Personality','Small talk support for MIKA. Trained to help with telecom specific tasks. An extra hour of productive time, every day.','','','Free',0,'Who is Mika?','2017-01-31 00:00:00','Other','https://www.hutoma.com/privacy.pdf','EVERYONE','1.0','',0,'_mika.png');
/*!40000 ALTER TABLE `botStore` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `hutoma`.`users` (`username`, `email`, `password`, `password_salt`, `first_name`, `dev_token`, `dev_id`, `client_token`, `last_name`) VALUES ('system-user', '', 'none', 'none', 'system user', '', '192a1677-551f-46bb-9188-04ed8a658926', 'none', '');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `entity` WRITE;
/*!40000 ALTER TABLE `entity` DISABLE KEYS */;
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.places', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.person', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.group', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.date', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.time', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.percent', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.number', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.ordinal', '2017-05-12 00:00:00', '1');
INSERT INTO `entity` (`dev_id`, `name`, `created`, `isSystem`) VALUES ('192a1677-551f-46bb-9188-04ed8a658926', 'sys.any', '2017-05-12 00:00:00', '1');
/*!40000 ALTER TABLE `entity` ENABLE KEYS */;
UNLOCK TABLES;
