CREATE TABLE IF NOT EXISTS `bbs_clannotice` (
	`clan_id` INT UNSIGNED NOT NULL,
	`type` SMALLINT NOT NULL DEFAULT '0',
	`notice` text NOT NULL,
	PRIMARY KEY(`clan_id`,`type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;