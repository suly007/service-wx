/*
 Navicat Premium Data Transfer

 Source Server         : 94.191.4.32
 Source Server Type    : MySQL
 Source Server Version : 50641
 Source Host           : 94.191.4.32:4332
 Source Schema         : weixin

 Target Server Type    : MySQL
 Target Server Version : 50641
 File Encoding         : 65001

 Date: 23/10/2018 12:51:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for stocks_list
-- ----------------------------
DROP TABLE IF EXISTS `stocks_list`;
CREATE TABLE `stocks_list`  (
  `stocks_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `open_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '微信号ID',
  `stocks_code` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '编码',
  `stocks_alias` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '别名',
  `stocks_price_init` double(16, 8) NOT NULL DEFAULT 0.00000000 COMMENT '初始价格',
  `stocks_code_comp` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '对比编码',
  `stocks_alias_comp` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '对比别名',
  `stocks_price_init_comp` double(16, 8) NOT NULL DEFAULT 0.00000000 COMMENT '初始价格-对比',
  `base_multiple` int(2) NOT NULL DEFAULT 1 COMMENT '倍数',
  `diff_warn_process_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '处理状态',
  `diff_warn_time` datetime(0) DEFAULT NULL COMMENT '上次预警时间(对比)',
  `diff_range` double(16, 8) DEFAULT NULL COMMENT '变动范围',
  `change_min` double(16, 8) DEFAULT -2.00000000 COMMENT '小值',
  `change_max` double(16, 8) DEFAULT 2.00000000 COMMENT '大值',
  `add_date` datetime(0) DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `modify_date` datetime(0) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `appid` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '公众号ID',
  PRIMARY KEY (`stocks_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '自选信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for weixin_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `weixin_blacklist`;
CREATE TABLE `weixin_blacklist`  (
  `open_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`open_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '黑名单' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for weixin_message
-- ----------------------------
DROP TABLE IF EXISTS `weixin_message`;
CREATE TABLE `weixin_message`  (
  `message_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `to_open_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '微信账号',
  `to_user` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '姓名',
  `message` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '消息',
  `send_res` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '发送结果',
  `send_time` datetime(0) DEFAULT NULL COMMENT '发送时间',
  `add_date` datetime(0) DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`message_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 352 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '微信access_token信息' ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
