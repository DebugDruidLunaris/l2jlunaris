CREATE TABLE IF NOT EXISTS `character_secondary_password` (
	`account_name` VARCHAR(45) NOT NULL DEFAULT '',
	`var`  VARCHAR(20) NOT NULL DEFAULT '',
	`value` VARCHAR(255) ,
	PRIMARY KEY (`account_name`,`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;