CREATE TABLE IF NOT EXISTS `siege_clans` (
	`residence_id` int(11) NOT NULL DEFAULT '0',
	`clan_id` int(11) NOT NULL DEFAULT '0',
	`type` varchar(255) NOT NULL,
	`param` bigint(20) NOT NULL,
	`date` bigint(20) NOT NULL,
	PRIMARY KEY (`residence_id`,`clan_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;