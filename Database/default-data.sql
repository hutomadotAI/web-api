
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
	(2,'Facebook Messenger','An easier way to message','messenger.png',1),
	(3,'Siri','Virtual assistant created by Apple','siri.png',1),
	(4,'Slack','A messaging app for teams','slack.png',1),
	(5,'Telegram','A new era of messaging','telegram.png',1),
	(6,'Twilio','Programmable SMS','twilio.png',0),
	(7,'Webhook','A way to plug a web service into your agent','webhook.png',1),
	(8,'WeChat','A text and voice messaging communication','wechat.png',0),
	(9,'WhatsApp','Cross-platform mobile messaging','whatsapp.png',0);
/*!40000 ALTER TABLE `integrations` ENABLE KEYS */;
UNLOCK TABLES;

