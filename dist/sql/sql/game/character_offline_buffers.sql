/*
Navicat MySQL Data Transfer

Source Server         : GVE
Source Server Version : 50535
Source Host           : 91.223.133.18:3306
Source Database       : l2jdb

Target Server Type    : MYSQL
Target Server Version : 50535
File Encoding         : 65001

Date: 2014-03-31 00:09:03
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for character_offline_buffers
-- ----------------------------
DROP TABLE IF EXISTS `character_offline_buffers`;
CREATE TABLE `character_offline_buffers` (
  `charId` int(10) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `price` int(10) DEFAULT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
