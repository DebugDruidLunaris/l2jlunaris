CREATE TABLE IF NOT EXISTS `siege_players` (
	`residence_id` int(11) NOT NULL,
	`object_id` int(11) NOT NULL,
	`clan_id` int(11) NOT NULL,
	PRIMARY KEY (`residence_id`,`object_id`,`clan_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;