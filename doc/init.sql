-- 网关应用信息
CREATE TABLE `tiga_app_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '应用名称',
  `desc` varchar(200) NOT NULL DEFAULT '' COMMENT '应用描述',
  `type` tinyint(2) NOT NULL DEFAULT '0' COMMENT '应用类型 1 PC 2APP',
  `app_key` varchar(30) NOT NULL DEFAULT '' COMMENT '应用Key',
  `app_secret` varchar(100) NOT NULL DEFAULT '' COMMENT '应用Secret',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '应用状态 0不可用 1可用',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT '1970-01-02 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_key` (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关应用信息';


-- 网关授权服务信息
CREATE TABLE `tiga_app_server` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `server_code` varchar(30) NOT NULL COMMENT '服务编码',
  `app_key` varchar(30) NOT NULL COMMENT '应用Key',
  `server_ips` varchar(500) NOT NULL COMMENT '服务授权IP地址',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '服务状态 0不可用 1可用',
  `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `mtime` timestamp NOT NULL DEFAULT '1970-01-02 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `app_key` (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关授权服务信息';


-- 初始化数据
INSERT INTO `tiga_app_info` (`id`, `name`, `desc`, `type`, `app_key`, `app_secret`, `status`, `ctime`, `mtime`)
VALUES
    (1, '1234567890', '1234567890', 0, '1234567890', '11111111', 1, '2019-03-07 16:14:55', '2019-03-07 16:30:45');
