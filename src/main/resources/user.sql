-- 创建用户,数据库,授权sql
CREATE DATABASE `weixin` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
CREATE USER 'weixin'@'%' IDENTIFIED BY 'huan@weixin521';
grant all privileges on weixin.* to 'weixin'@'%';

CREATE USER 'weixin'@'localhost' IDENTIFIED BY 'huan@weixin521';
grant all privileges on weixin.* to 'weixin'@'localhost';
