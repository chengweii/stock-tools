/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50096
Source Host           : localhost:3306
Source Database       : stock

Target Server Type    : MYSQL
Target Server Version : 50096
File Encoding         : 65001

Date: 2017-07-19 14:13:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for enterprise_info
-- ----------------------------
DROP TABLE IF EXISTS `enterprise_info`;
CREATE TABLE `enterprise_info` (
  `id` int(11) NOT NULL auto_increment,
  `stock_code` varchar(50) default NULL,
  `ssdq` varchar(50) default NULL COMMENT '所属地区',
  `zgb` decimal(50,5) default NULL COMMENT '总股本(亿)',
  `mgsy` decimal(50,5) default NULL COMMENT '每股收益(元)',
  `sssj` varchar(20) default NULL COMMENT '上市时间',
  `ltga` decimal(50,5) default NULL COMMENT '流通A股(亿)',
  `mgjzc` decimal(50,5) default NULL,
  `mgxjl` decimal(50,5) default NULL,
  `jzcsyl` decimal(10,5) default NULL,
  `jlrzzl` decimal(10,5) default NULL,
  `mgwfplr` decimal(10,5) default NULL,
  `zysrzzl` decimal(10,5) default NULL,
  `syl` decimal(10,5) default NULL COMMENT '市盈率',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3501 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for stock_list
-- ----------------------------
DROP TABLE IF EXISTS `stock_list`;
CREATE TABLE `stock_list` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(20) default NULL,
  `code` varchar(50) default NULL,
  `status` tinyint(4) NOT NULL default '1' COMMENT '0 基金；1 正常；2 休市；3 退市；',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4597 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for stock_plate
-- ----------------------------
DROP TABLE IF EXISTS `stock_plate`;
CREATE TABLE `stock_plate` (
  `stock_code` varchar(50) NOT NULL,
  `plate_name` varchar(100) default NULL,
  `plate_code` varchar(50) NOT NULL,
  PRIMARY KEY  (`stock_code`,`plate_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
