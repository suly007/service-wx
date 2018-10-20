/*
 Navicat Premium Data Transfer

 Source Server         : 118.25.227.182
 Source Server Type    : MySQL
 Source Server Version : 50641
 Source Host           : 118.25.227.182:4332
 Source Schema         : weixin

 Target Server Type    : MySQL
 Target Server Version : 50641
 File Encoding         : 65001

 Date: 19/10/2018 22:12:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for stocks_list
-- ----------------------------
DROP TABLE IF EXISTS `stocks_list`;
CREATE TABLE `stocks_list`  (
  `stocks_id` int(11) NOT NULL AUTO_INCREMENT,
  `open_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '微信号ID',
  `stocks_code` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `stocks_alias` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `stocks_code_comp` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `stocks_alias_comp` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `base_diff` double(255, 0) DEFAULT NULL,
  `diff_warn_time` datetime(0) DEFAULT NULL,
  `diff_range` double(255, 0) DEFAULT NULL,
  `change_min` double(255, 0) DEFAULT -2,
  `change_min_flag` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `change_max` double(255, 0) DEFAULT 2,
  `change_max_flag` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `add_userid` int(11) DEFAULT NULL,
  `add_date` datetime(0) DEFAULT CURRENT_TIMESTAMP,
  `modify_userid` int(11) DEFAULT NULL,
  `modify_date` datetime(0) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `appid` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '公众号ID',
  PRIMARY KEY (`stocks_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for weixin_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `weixin_blacklist`;
CREATE TABLE `weixin_blacklist`  (
  `open_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`open_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for weixin_message
-- ----------------------------
DROP TABLE IF EXISTS `weixin_message`;
CREATE TABLE `weixin_message`  (
  `message_id` int(11) NOT NULL AUTO_INCREMENT,
  `to_open_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `to_user` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `message` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `send_res` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `send_time` datetime(0) DEFAULT NULL,
  `add_date` datetime(0) DEFAULT NULL,
  PRIMARY KEY (`message_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for weixin_token
-- ----------------------------
DROP TABLE IF EXISTS `weixin_token`;
CREATE TABLE `weixin_token`  (
  `token_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键,自增',
  `appid` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '开发者ID',
  `appsecret` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '开发者密码',
  `token` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'access_token',
  `expiresin` int(10) DEFAULT NULL COMMENT '超时时长(S)',
  `expiresdate` datetime(0) DEFAULT NULL COMMENT '超时时间',
  `add_date` datetime(0) DEFAULT CURRENT_TIMESTAMP COMMENT '获取时间',
  PRIMARY KEY (`token_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
