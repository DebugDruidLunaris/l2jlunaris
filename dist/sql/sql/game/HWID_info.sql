DROP TABLE IF EXISTS `hwid_info`;
CREATE TABLE `hwid_info` (
`HWID` varchar(32) NOT NULL default '',
`HWIDSecond` varchar(32) NOT NULL default '',
`WindowsCount` INT UNSIGNED NOT NULL DEFAULT 1,
`Account` varchar(45) NOT NULL default '',
`PlayerID` INT UNSIGNED NOT NULL DEFAULT 0,
`LockType` enum('PLAYER_LOCK','ACCOUNT_LOCK','NONE') NOT NULL default 'NONE',
PRIMARY KEY  (`HWID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;