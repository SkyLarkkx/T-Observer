-- 教师听课评课记录与分析系统 MySQL DDL
-- 文档依据：
-- 1. doc/教师听课评课记录与分析系统需求规格说明书.md
-- 2. doc/教师听课评课记录与分析系统系统架构设计文档.md
-- 3. doc/教师听课评课记录与分析系统数据库设计文档.md
-- 目标数据库：MySQL 8.x

CREATE DATABASE IF NOT EXISTS `t_observer_core`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `t_observer_core`;

-- =========================
-- 认证与权限域
-- =========================

CREATE TABLE IF NOT EXISTS `auth_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint unsigned NOT NULL COMMENT '外部统一用户标识',
  `username` varchar(64) NOT NULL DEFAULT '' COMMENT '登录名或工号',
  `real_name` varchar(64) NOT NULL DEFAULT '' COMMENT '姓名',
  `mobile` varchar(32) NOT NULL DEFAULT '' COMMENT '手机号',
  `email` varchar(128) NOT NULL DEFAULT '' COMMENT '邮箱',
  `org_unit_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '所属组织单元主键',
  `user_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态',
  `source_system` varchar(32) NOT NULL DEFAULT 'SSO' COMMENT '来源系统',
  `last_login_at` datetime DEFAULT NULL COMMENT '最近登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_auth_user` PRIMARY KEY (`id`),
  CONSTRAINT `uk_user_uid` UNIQUE KEY (`user_id`),
  KEY `idx_user_org_status` (`org_unit_id`, `user_status`),
  KEY `idx_user_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户主数据快照表';

CREATE TABLE IF NOT EXISTS `auth_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码',
  `role_name` varchar(64) NOT NULL DEFAULT '' COMMENT '角色名称',
  `role_scope` varchar(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '角色范围',
  `role_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '角色状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_auth_role` PRIMARY KEY (`id`),
  CONSTRAINT `uk_role_code` UNIQUE KEY (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色定义表';

CREATE TABLE IF NOT EXISTS `auth_permission` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `permission_code` varchar(64) NOT NULL COMMENT '权限编码',
  `permission_name` varchar(64) NOT NULL DEFAULT '' COMMENT '权限名称',
  `permission_type` varchar(32) NOT NULL DEFAULT 'API' COMMENT '权限类型',
  `resource_path` varchar(255) NOT NULL DEFAULT '' COMMENT '资源路径',
  `http_method` varchar(16) NOT NULL DEFAULT '' COMMENT 'HTTP方法',
  `permission_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '权限状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_auth_permission` PRIMARY KEY (`id`),
  CONSTRAINT `uk_perm_code` UNIQUE KEY (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限点定义表';

CREATE TABLE IF NOT EXISTS `auth_user_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户业务标识',
  `role_id` bigint unsigned NOT NULL COMMENT '角色主键',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_auth_user_role` PRIMARY KEY (`id`),
  CONSTRAINT `uk_user_role` UNIQUE KEY (`user_id`, `role_id`),
  KEY `idx_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `auth_role_permission` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `role_id` bigint unsigned NOT NULL COMMENT '角色主键',
  `permission_id` bigint unsigned NOT NULL COMMENT '权限主键',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_auth_role_permission` PRIMARY KEY (`id`),
  CONSTRAINT `uk_role_perm` UNIQUE KEY (`role_id`, `permission_id`),
  KEY `idx_role_perm_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

-- =========================
-- 主数据域
-- =========================

CREATE TABLE IF NOT EXISTS `md_org_unit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `org_unit_id` bigint unsigned NOT NULL COMMENT '外部组织标识',
  `parent_org_unit_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '父级组织标识',
  `org_name` varchar(128) NOT NULL DEFAULT '' COMMENT '组织名称',
  `org_type` varchar(32) NOT NULL DEFAULT 'GROUP' COMMENT '组织类型',
  `subject_code` varchar(32) NOT NULL DEFAULT '' COMMENT '归属学科编码',
  `leader_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '组长用户标识',
  `org_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '组织状态',
  `sort_no` int unsigned NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_md_org_unit` PRIMARY KEY (`id`),
  CONSTRAINT `uk_org_oid` UNIQUE KEY (`org_unit_id`),
  KEY `idx_org_parent_sort` (`parent_org_unit_id`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织单元表';

CREATE TABLE IF NOT EXISTS `md_subject` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `subject_code` varchar(32) NOT NULL COMMENT '学科编码',
  `subject_name` varchar(64) NOT NULL DEFAULT '' COMMENT '学科名称',
  `subject_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '学科状态',
  `sort_no` int unsigned NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_md_subject` PRIMARY KEY (`id`),
  CONSTRAINT `uk_subject_code` UNIQUE KEY (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学科字典表';

CREATE TABLE IF NOT EXISTS `md_eval_dimension` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `dimension_code` varchar(32) NOT NULL COMMENT '维度编码',
  `dimension_name` varchar(64) NOT NULL DEFAULT '' COMMENT '维度名称',
  `dimension_desc` varchar(512) NOT NULL DEFAULT '' COMMENT '维度说明',
  `score_weight` decimal(5,2) NOT NULL DEFAULT 1.00 COMMENT '维度权重',
  `min_score` decimal(2,1) NOT NULL DEFAULT 1.0 COMMENT '最小分值',
  `max_score` decimal(2,1) NOT NULL DEFAULT 5.0 COMMENT '最大分值',
  `score_step` decimal(2,1) NOT NULL DEFAULT 0.5 COMMENT '步长',
  `is_radar_enabled` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否参与雷达图',
  `dimension_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '维度状态',
  `sort_no` int unsigned NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_md_eval_dimension` PRIMARY KEY (`id`),
  CONSTRAINT `uk_dim_code` UNIQUE KEY (`dimension_code`),
  CONSTRAINT `ck_dim_weight` CHECK (`score_weight` > 0),
  CONSTRAINT `ck_dim_min_max` CHECK (`min_score` <= `max_score`),
  CONSTRAINT `ck_dim_step` CHECK (`score_step` > 0),
  CONSTRAINT `ck_dim_radar_enabled` CHECK (`is_radar_enabled` IN (0, 1)),
  KEY `idx_dim_status_sort` (`dimension_status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评课维度字典表';

-- =========================
-- 听课任务域
-- =========================

CREATE TABLE IF NOT EXISTS `task_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `task_code` varchar(32) NOT NULL COMMENT '任务编号',
  `task_name` varchar(128) NOT NULL DEFAULT '' COMMENT '任务名称',
  `org_unit_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '教研组主键',
  `leader_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '任务负责人用户标识',
  `teacher_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '被听课教师用户标识',
  `subject_code` varchar(32) NOT NULL DEFAULT '' COMMENT '学科编码',
  `grade_name` varchar(64) NOT NULL DEFAULT '' COMMENT '年级名称',
  `class_name` varchar(64) NOT NULL DEFAULT '' COMMENT '班级名称',
  `course_name` varchar(128) NOT NULL DEFAULT '' COMMENT '课程名称',
  `lesson_instance_code` varchar(64) NOT NULL COMMENT '课程实例编码',
  `lesson_date` datetime NOT NULL COMMENT '听课时间',
  `submit_deadline` datetime NOT NULL COMMENT '提交截止时间',
  `rectify_deadline` datetime NOT NULL COMMENT '退回整改截止时间',
  `task_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '任务状态',
  `publish_at` datetime DEFAULT NULL COMMENT '发布时间',
  `archived_at` datetime DEFAULT NULL COMMENT '归档时间',
  `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '任务备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_task_plan` PRIMARY KEY (`id`),
  CONSTRAINT `uk_task_code` UNIQUE KEY (`task_code`),
  CONSTRAINT `uk_task_teacher_lesson` UNIQUE KEY (`teacher_user_id`, `lesson_instance_code`),
  CONSTRAINT `ck_task_time_order` CHECK (`submit_deadline` >= `lesson_date`),
  CONSTRAINT `ck_task_rectify_time` CHECK (`rectify_deadline` >= `submit_deadline`),
  KEY `idx_task_org_status` (`org_unit_id`, `task_status`),
  KEY `idx_task_leader_status` (`leader_user_id`, `task_status`),
  KEY `idx_task_teacher_date` (`teacher_user_id`, `lesson_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='听课任务主表';

CREATE TABLE IF NOT EXISTS `task_assignment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `task_plan_id` bigint unsigned NOT NULL COMMENT '任务主表主键',
  `observer_user_id` bigint unsigned NOT NULL COMMENT '听课人用户标识',
  `lesson_instance_code` varchar(64) NOT NULL DEFAULT '' COMMENT '课程实例编码',
  `assignment_status` varchar(32) NOT NULL DEFAULT 'ASSIGNED' COMMENT '分配状态',
  `receive_at` datetime DEFAULT NULL COMMENT '接收时间',
  `submitted_at` datetime DEFAULT NULL COMMENT '提交时间',
  `approved_at` datetime DEFAULT NULL COMMENT '审核通过时间',
  `archived_at` datetime DEFAULT NULL COMMENT '归档时间',
  `remind_count` int unsigned NOT NULL DEFAULT 0 COMMENT '提醒次数',
  `last_remind_at` datetime DEFAULT NULL COMMENT '最后提醒时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_task_assignment` PRIMARY KEY (`id`),
  CONSTRAINT `uk_assign_plan_observer` UNIQUE KEY (`task_plan_id`, `observer_user_id`),
  CONSTRAINT `uk_assign_observer_lesson` UNIQUE KEY (`observer_user_id`, `lesson_instance_code`),
  KEY `idx_assign_observer_status` (`observer_user_id`, `assignment_status`),
  KEY `idx_assign_status_submit` (`assignment_status`, `submitted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='听课任务分配明细表';

-- =========================
-- 听课记录域
-- =========================

CREATE TABLE IF NOT EXISTS `record_observation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `record_no` varchar(32) NOT NULL COMMENT '记录编号',
  `assignment_id` bigint unsigned NOT NULL COMMENT '任务分配明细主键',
  `task_plan_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '任务主表主键',
  `observer_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '听课人用户标识',
  `teacher_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '被听课教师用户标识',
  `record_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '记录状态',
  `submit_version` int unsigned NOT NULL DEFAULT 0 COMMENT '提交版本号',
  `is_overtime_submit` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否逾期提交',
  `overdue_reason` varchar(500) NOT NULL DEFAULT '' COMMENT '逾期提交原因',
  `return_reason` varchar(1000) NOT NULL DEFAULT '' COMMENT '最近一次退回原因',
  `submitted_at` datetime DEFAULT NULL COMMENT '提交时间',
  `returned_at` datetime DEFAULT NULL COMMENT '退回时间',
  `approved_at` datetime DEFAULT NULL COMMENT '审核通过时间',
  `archived_at` datetime DEFAULT NULL COMMENT '归档时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_record_observation` PRIMARY KEY (`id`),
  CONSTRAINT `uk_record_no` UNIQUE KEY (`record_no`),
  CONSTRAINT `uk_record_assignment` UNIQUE KEY (`assignment_id`),
  CONSTRAINT `ck_record_overtime_submit` CHECK (`is_overtime_submit` IN (0, 1)),
  KEY `idx_record_teacher_status` (`teacher_user_id`, `record_status`),
  KEY `idx_record_status_time` (`record_status`, `submitted_at`),
  KEY `idx_record_observer_status` (`observer_user_id`, `record_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='听课记录主表';

CREATE TABLE IF NOT EXISTS `record_text_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `record_id` bigint unsigned NOT NULL COMMENT '听课记录主键',
  `strengths_text` text NOT NULL COMMENT '优点项文本',
  `weaknesses_text` text NOT NULL COMMENT '待改进项文本',
  `suggestion_text` text NOT NULL COMMENT '改进建议文本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_record_text_detail` PRIMARY KEY (`id`),
  CONSTRAINT `uk_text_record` UNIQUE KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='听课记录长文本详情表';

CREATE TABLE IF NOT EXISTS `record_score` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `record_id` bigint unsigned NOT NULL COMMENT '听课记录主键',
  `dimension_code` varchar(32) NOT NULL COMMENT '评课维度编码',
  `score_value` decimal(2,1) NOT NULL DEFAULT 1.0 COMMENT '维度评分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_record_score` PRIMARY KEY (`id`),
  CONSTRAINT `uk_score_record_dim` UNIQUE KEY (`record_id`, `dimension_code`),
  CONSTRAINT `ck_score_value` CHECK (`score_value` IN (1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)),
  KEY `idx_score_dim` (`dimension_code`),
  KEY `idx_score_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='听课记录评分明细表';

-- =========================
-- 审核归档域
-- =========================

CREATE TABLE IF NOT EXISTS `review_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `record_id` bigint unsigned NOT NULL COMMENT '听课记录主键',
  `review_round` int unsigned NOT NULL DEFAULT 1 COMMENT '审核轮次',
  `reviewer_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '审核人用户标识',
  `action_code` varchar(32) NOT NULL DEFAULT '' COMMENT '审核动作',
  `before_status` varchar(32) NOT NULL DEFAULT '' COMMENT '变更前状态',
  `after_status` varchar(32) NOT NULL DEFAULT '' COMMENT '变更后状态',
  `review_reason` varchar(1000) NOT NULL DEFAULT '' COMMENT '审核意见或退回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  CONSTRAINT `pk_review_log` PRIMARY KEY (`id`),
  KEY `idx_review_record_time` (`record_id`, `create_time`),
  KEY `idx_review_reviewer_time` (`reviewer_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审核与归档日志表';

-- =========================
-- 分析报告域
-- =========================

CREATE TABLE IF NOT EXISTS `report_radar` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `report_no` varchar(32) NOT NULL COMMENT '报告编号',
  `teacher_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '被听课教师用户标识',
  `org_unit_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '组织单元主键',
  `subject_code` varchar(32) NOT NULL DEFAULT '' COMMENT '学科编码',
  `period_type` varchar(32) NOT NULL DEFAULT 'CUSTOM' COMMENT '周期类型',
  `period_start` date NOT NULL COMMENT '周期开始日期',
  `period_end` date NOT NULL COMMENT '周期结束日期',
  `sample_count` int unsigned NOT NULL DEFAULT 0 COMMENT '样本数量',
  `is_sample_sufficient` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否满足最小样本量',
  `current_version_no` int unsigned NOT NULL DEFAULT 1 COMMENT '当前版本号',
  `report_status` varchar(32) NOT NULL DEFAULT 'GENERATED' COMMENT '报告状态',
  `conclusion_text` varchar(2000) NOT NULL DEFAULT '' COMMENT '结论摘要',
  `radar_json` json DEFAULT NULL COMMENT '雷达图聚合数据',
  `generated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_report_radar` PRIMARY KEY (`id`),
  CONSTRAINT `uk_report_no` UNIQUE KEY (`report_no`),
  CONSTRAINT `uk_report_period` UNIQUE KEY (`teacher_user_id`, `period_type`, `period_start`, `period_end`),
  CONSTRAINT `ck_report_period` CHECK (`period_end` >= `period_start`),
  CONSTRAINT `ck_report_sample_count` CHECK (`sample_count` >= 0),
  CONSTRAINT `ck_report_sample_sufficient` CHECK (`is_sample_sufficient` IN (0, 1)),
  KEY `idx_report_teacher_status` (`teacher_user_id`, `report_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='当前有效雷达分析报告表';

CREATE TABLE IF NOT EXISTS `report_version` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `report_id` bigint unsigned NOT NULL COMMENT '报告主表主键',
  `version_no` int unsigned NOT NULL DEFAULT 1 COMMENT '版本号',
  `sample_count` int unsigned NOT NULL DEFAULT 0 COMMENT '样本数量',
  `is_sample_sufficient` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否满足样本量',
  `change_reason` varchar(500) NOT NULL DEFAULT '' COMMENT '版本变更原因',
  `conclusion_text` varchar(2000) NOT NULL DEFAULT '' COMMENT '版本结论',
  `radar_json` json DEFAULT NULL COMMENT '版本雷达图数据',
  `generated_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '生成或修正操作人',
  `published_at` datetime DEFAULT NULL COMMENT '版本发布时间',
  `is_current` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否当前有效版本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_report_version` PRIMARY KEY (`id`),
  CONSTRAINT `uk_report_ver` UNIQUE KEY (`report_id`, `version_no`),
  CONSTRAINT `ck_report_ver_sample` CHECK (`sample_count` >= 0),
  CONSTRAINT `ck_report_ver_sufficient` CHECK (`is_sample_sufficient` IN (0, 1)),
  CONSTRAINT `ck_report_ver_current` CHECK (`is_current` IN (0, 1)),
  KEY `idx_report_ver_current` (`report_id`, `is_current`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分析报告历史版本表';

CREATE TABLE IF NOT EXISTS `report_feedback_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `report_version_id` bigint unsigned NOT NULL COMMENT '报告版本主键',
  `item_type` varchar(32) NOT NULL DEFAULT '' COMMENT '反馈项类型',
  `item_content` varchar(1000) NOT NULL DEFAULT '' COMMENT '反馈项内容',
  `source_count` int unsigned NOT NULL DEFAULT 0 COMMENT '来源记录数量',
  `order_no` int unsigned NOT NULL DEFAULT 0 COMMENT '展示顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `pk_report_feedback_item` PRIMARY KEY (`id`),
  KEY `idx_feedback_ver_type` (`report_version_id`, `item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报告优缺点汇总项表';

-- =========================
-- 申诉复核域
-- =========================

CREATE TABLE IF NOT EXISTS `appeal_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `appeal_no` varchar(32) NOT NULL COMMENT '申诉编号',
  `report_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '报告主表主键',
  `report_version_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '被申诉报告版本主键',
  `teacher_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '申诉教师用户标识',
  `reviewer_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '复核人用户标识',
  `appeal_status` varchar(32) NOT NULL DEFAULT 'SUBMITTED' COMMENT '申诉状态',
  `is_overtime` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否超时提交',
  `appeal_reason` varchar(2000) NOT NULL DEFAULT '' COMMENT '申诉说明',
  `review_result` varchar(1000) NOT NULL DEFAULT '' COMMENT '复核结论',
  `corrected_report_version_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '修正后报告版本主键',
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申诉提交时间',
  `reviewed_at` datetime DEFAULT NULL COMMENT '复核完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `pk_appeal_order` PRIMARY KEY (`id`),
  CONSTRAINT `uk_appeal_no` UNIQUE KEY (`appeal_no`),
  CONSTRAINT `ck_appeal_overtime` CHECK (`is_overtime` IN (0, 1)),
  KEY `idx_appeal_report_status` (`report_id`, `appeal_status`),
  KEY `idx_appeal_teacher_status` (`teacher_user_id`, `appeal_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='申诉复核单据表';

-- =========================
-- 通知与审计域
-- =========================

CREATE TABLE IF NOT EXISTS `notice_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `biz_type` varchar(32) NOT NULL DEFAULT '' COMMENT '业务类型',
  `biz_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '业务主键',
  `receiver_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '接收人用户标识',
  `notice_channel` varchar(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '通知渠道',
  `template_code` varchar(64) NOT NULL DEFAULT '' COMMENT '模板编码',
  `notice_title` varchar(128) NOT NULL DEFAULT '' COMMENT '通知标题',
  `notice_content` varchar(1000) NOT NULL DEFAULT '' COMMENT '通知内容摘要',
  `notice_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态',
  `trace_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪号',
  `send_at` datetime DEFAULT NULL COMMENT '发送时间',
  `callback_at` datetime DEFAULT NULL COMMENT '回执时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `pk_notice_log` PRIMARY KEY (`id`),
  KEY `idx_notice_biz` (`biz_type`, `biz_id`),
  KEY `idx_notice_receiver_status` (`receiver_user_id`, `notice_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知发送记录表';

CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `biz_type` varchar(32) NOT NULL DEFAULT '' COMMENT '业务类型',
  `biz_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '业务主键',
  `operation_code` varchar(32) NOT NULL DEFAULT '' COMMENT '操作编码',
  `operator_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '操作人用户标识',
  `operator_role_code` varchar(32) NOT NULL DEFAULT '' COMMENT '操作时角色编码',
  `before_status` varchar(32) NOT NULL DEFAULT '' COMMENT '变更前状态',
  `after_status` varchar(32) NOT NULL DEFAULT '' COMMENT '变更后状态',
  `before_json` json DEFAULT NULL COMMENT '变更前快照',
  `after_json` json DEFAULT NULL COMMENT '变更后快照',
  `request_ip` varchar(45) NOT NULL DEFAULT '' COMMENT '请求IP地址',
  `terminal_info` varchar(255) NOT NULL DEFAULT '' COMMENT '终端信息',
  `trace_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `pk_audit_log` PRIMARY KEY (`id`),
  KEY `idx_audit_biz` (`biz_type`, `biz_id`),
  KEY `idx_audit_operator_time` (`operator_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关键操作审计日志表';
