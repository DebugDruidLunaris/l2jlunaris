CREATE TABLE IF NOT EXISTS `gameservers` (
	`server_id` int(11) NOT NULL,
	`host` varchar(255) NOT NULL,
	PRIMARY KEY (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;