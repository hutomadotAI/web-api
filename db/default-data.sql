
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
INSERT INTO `users` VALUES ('hutoma@hutoma.com','hutoma@hutoma.com','none','none','Hutoma','2017-01-31 18:15:14','0','',1,'d14b441b-e2a2-4b9f-be9b-4301647e1265','','',1,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `ai` WRITE;
/*!40000 ALTER TABLE `ai` DISABLE KEYS */;
INSERT INTO `ai` VALUES (1,'e1bb8226-e8ce-467a-8305-bc2fcb89dd7f','AIML','','2017-01-31 18:10:17','d14b441b-e2a2-4b9f-be9b-4301647e1265',0,'eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNocijEKwzAMAP-iuQLZUhypW2kyGEwCpUunEtnpB0qn0L_XdDqOuwNua5nh_MfzWvK83NelPOAEl5ynHppH8tEZnbShmBFuMURsQ5K98qbCr36_P95nNe4eAlKyhCLGqFUTEqt5lXF3GeD7AwAA__8.vLFd5SSUH37G5aFq7byG5ZBDZiX5XHs2RJLS_MfWLhg','{\"engines\": {}}','en-US','Europe/London',0.4000000059604645,0,0,0);
/*!40000 ALTER TABLE `ai` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `botStore` WRITE;
/*!40000 ALTER TABLE `botStore` DISABLE KEYS */;
INSERT INTO `botStore` VALUES (1,'d14b441b-e2a2-4b9f-be9b-4301647e1265','e1bb8226-e8ce-467a-8305-bc2fcb89dd7f','Chit-Chat','Free Chit-Chat bot','Just include this bot in your AI to get instant chit-chat','','','Free',0,'','2017-01-31 00:00:00','Other','','EVERYONE','1.0','',1,null);
/*!40000 ALTER TABLE `botStore` ENABLE KEYS */;
UNLOCK TABLES;