-- 执行器注册表
CREATE TABLE IF NOT EXISTS `xl_job_registry` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `registry_group` varchar(50) NOT NULL COMMENT '执行器组名',
  `registry_key` varchar(255) NOT NULL COMMENT '执行器标识',
  `registry_value` varchar(255) NOT NULL COMMENT '执行器地址(IP:Port)',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_registry_group` (`registry_group`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行器注册表';

-- 执行器组表
CREATE TABLE IF NOT EXISTS `xl_job_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) NOT NULL COMMENT '执行器AppName',
  `title` varchar(64) NOT NULL COMMENT '执行器名称',
  `address_type` tinyint NOT NULL DEFAULT 0 COMMENT '地址类型:0-自动注册,1-手动录入',
  `address_list` text COMMENT '执行器地址列表',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_name` (`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行器组表';

-- 任务信息表
CREATE TABLE IF NOT EXISTS `xl_job_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_group` varchar(50) NOT NULL COMMENT '执行器组名',
  `job_desc` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `schedule_type` tinyint NOT NULL DEFAULT 1 COMMENT '调度类型:1-CRON',
  `schedule_conf` varchar(128) DEFAULT NULL COMMENT 'Cron表达式',
  `misfire_strategy` tinyint NOT NULL DEFAULT 1 COMMENT '错过的策略:1-立即执行,2-忽略',
  `executor_route_strategy` varchar(50) DEFAULT 'ROUND' COMMENT '路由策略',
  `executor_handler` varchar(255) NOT NULL COMMENT '任务Handler',
  `executor_param` varchar(512) DEFAULT NULL COMMENT '任务参数',
  `executor_block_strategy` varchar(50) DEFAULT 'SERIAL' COMMENT '阻塞策略',
  `executor_timeout` int NOT NULL DEFAULT 0 COMMENT '超时时间(秒)',
  `executor_fail_retry_count` int NOT NULL DEFAULT 0 COMMENT '失败重试次数',
  `glue_type` varchar(50) NOT NULL DEFAULT 'BEAN' COMMENT '任务类型',
  `glue_source` mediumtext COMMENT '脚本源码',
  `glue_remark` varchar(128) DEFAULT NULL COMMENT '脚本备注',
  `glue_updatetime` datetime DEFAULT NULL COMMENT '脚本更新时间',
  `child_jobid` varchar(255) DEFAULT NULL COMMENT '子任务ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态:0-停止,1-运行',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_group` (`job_group`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

-- 执行日志表
CREATE TABLE IF NOT EXISTS `xl_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_group` varchar(50) NOT NULL COMMENT '执行器组名',
  `job_id` bigint NOT NULL COMMENT '任务ID',
  `executor_address` varchar(255) DEFAULT NULL COMMENT '执行器地址',
  `executor_handler` varchar(255) DEFAULT NULL COMMENT '任务Handler',
  `executor_param` varchar(512) DEFAULT NULL COMMENT '任务参数',
  `executor_sharding_param` varchar(20) DEFAULT NULL COMMENT '分片参数',
  `executor_fail_retry_count` int NOT NULL DEFAULT 0 COMMENT '失败重试次数',
  `trigger_time` datetime DEFAULT NULL COMMENT '调度时间',
  `trigger_code` int DEFAULT NULL COMMENT '调度结果码',
  `trigger_msg` text COMMENT '调度日志',
  `handle_time` datetime DEFAULT NULL COMMENT '执行时间',
  `handle_code` int DEFAULT NULL COMMENT '执行结果码',
  `handle_msg` text COMMENT '执行日志',
  `alarm_status` tinyint NOT NULL DEFAULT 0 COMMENT '告警状态:0-默认,1-无需告警,2-已告警',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_group` (`job_group`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_trigger_time` (`trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行日志表';
