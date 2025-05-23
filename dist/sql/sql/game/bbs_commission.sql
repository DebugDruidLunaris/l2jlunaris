DROP TABLE IF EXISTS `bbs_commission`;
CREATE TABLE IF NOT EXISTS `bbs_commission` (
	`id` bigint(9) NOT NULL AUTO_INCREMENT,
	`owner_id` int(10) unsigned NOT NULL DEFAULT '0',
	`category` varchar(20) NOT NULL DEFAULT '-1',
	`item_id` int(7) unsigned NOT NULL DEFAULT '0',
	`item_name` varchar(255) NOT NULL,
	`item_count` bigint(100) unsigned NOT NULL DEFAULT '1',
	`enchant_level` int(11) unsigned NOT NULL DEFAULT '0',
	`augment_id` int(11) DEFAULT '0',
	`attribute_fire` int(11) DEFAULT '0',
	`attribute_water` int(11) DEFAULT '0',
	`attribute_wind` int(11) DEFAULT '0',
	`attribute_earth` int(11) DEFAULT '0',
	`attribute_holy` int(11) DEFAULT '0',
	`attribute_unholy` int(11) DEFAULT '0',
	`price_id` int(11) DEFAULT '0',
	`price_count` bigint(100) DEFAULT '0',
	`date` bigint(100) NOT NULL DEFAULT '0',
	`endtime` bigint(100) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
