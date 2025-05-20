CREATE TABLE IF NOT EXISTS `service_bonus` (
	`clan_name` varchar(100) DEFAULT NULL,
	`party_name` varchar(100) DEFAULT NULL,
	`hasReward` INT DEFAULT '0',
	PRIMARY KEY(`clan_name`,`party_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;