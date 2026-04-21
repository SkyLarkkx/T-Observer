insert into sys_user (id, username, password, real_name, role_code, status)
select 1, 'leader01', '123456', '张组长', 'LEADER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 1 or username = 'leader01');

insert into sys_user (id, username, password, real_name, role_code, status)
select 2, 'member01', '123456', '赵老师', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 2 or username = 'member01');

insert into sys_user (id, username, password, real_name, role_code, status)
select 3, 'member02', '123456', '王老师', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 3 or username = 'member02');

insert into sys_user (id, username, password, real_name, role_code, status)
select 4, 'member03', '123456', '李老师', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 4 or username = 'member03');

insert into sys_user (id, username, password, real_name, role_code, status)
select 5, 'admin01', '123456', '系统管理员', 'ADMIN', 'ACTIVE'
where not exists (select 1 from sys_user where id = 5 or username = 'admin01');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'TEACHING_DESIGN', '教学设计'
where not exists (select 1 from evaluation_dimension where dimension_code = 'TEACHING_DESIGN');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'CLASSROOM_ORGANIZATION', '课堂组织'
where not exists (select 1 from evaluation_dimension where dimension_code = 'CLASSROOM_ORGANIZATION');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'TEACHING_CONTENT', '教学内容'
where not exists (select 1 from evaluation_dimension where dimension_code = 'TEACHING_CONTENT');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'INTERACTION_FEEDBACK', '互动反馈'
where not exists (select 1 from evaluation_dimension where dimension_code = 'INTERACTION_FEEDBACK');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'TEACHING_EFFECTIVENESS', '教学效果'
where not exists (select 1 from evaluation_dimension where dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 1, '赵老师数学听课任务-待开始', 1, 2, '赵老师', '函数概念', '2026-04-20 09:00:00', '2026-04-22 18:00:00', 'PENDING', '待成员开始填写记录'
where not exists (select 1 from observation_task where id = 1);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 2, '王老师语文听课任务-待开始', 1, 3, '王老师', '议论文写作', '2026-04-20 14:00:00', '2026-04-23 18:00:00', 'PENDING', '覆盖 PENDING 任务状态'
where not exists (select 1 from observation_task where id = 2);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 3, '赵老师数学听课任务-草稿中', 1, 2, '赵老师', '二次函数', '2026-04-21 09:00:00', '2026-04-23 18:00:00', 'IN_PROGRESS', '已有草稿记录'
where not exists (select 1 from observation_task where id = 3);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 4, '王老师语文听课任务-已退回', 1, 3, '王老师', '古诗鉴赏', '2026-04-21 10:00:00', '2026-04-24 18:00:00', 'IN_PROGRESS', '记录被退回后继续修改'
where not exists (select 1 from observation_task where id = 4);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 5, '李老师英语听课任务-待评审', 1, 4, '李老师', '阅读理解', '2026-04-22 09:00:00', '2026-04-24 18:00:00', 'COMPLETED', '提交后等待组长评审'
where not exists (select 1 from observation_task where id = 5);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 6, '赵老师数学听课任务-已通过 1', 1, 2, '赵老师', '函数图像', '2026-04-07 09:00:00', '2026-04-09 18:00:00', 'COMPLETED', '赵老师分析样本 1'
where not exists (select 1 from observation_task where id = 6);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 7, '赵老师数学听课任务-已通过 2', 1, 3, '赵老师', '函数应用', '2026-04-12 09:00:00', '2026-04-14 18:00:00', 'COMPLETED', '赵老师分析样本 2'
where not exists (select 1 from observation_task where id = 7);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 8, '赵老师数学听课任务-已通过 3', 1, 4, '赵老师', '综合复习', '2026-04-18 09:00:00', '2026-04-20 18:00:00', 'COMPLETED', '赵老师分析样本 3'
where not exists (select 1 from observation_task where id = 8);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 9, '王老师语文听课任务-已通过 1', 1, 2, '王老师', '现代文阅读', '2026-04-08 14:00:00', '2026-04-10 18:00:00', 'COMPLETED', '王老师低样本分析 1'
where not exists (select 1 from observation_task where id = 9);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 10, '王老师语文听课任务-已通过 2', 1, 3, '王老师', '作文讲评', '2026-04-15 14:00:00', '2026-04-17 18:00:00', 'COMPLETED', '王老师低样本分析 2'
where not exists (select 1 from observation_task where id = 10);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 11, '李老师英语听课任务-已通过', 1, 4, '李老师', '听力训练', '2026-04-16 10:00:00', '2026-04-18 18:00:00', 'COMPLETED', '李老师低样本分析'
where not exists (select 1 from observation_task where id = 11);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 101, 3, 2, '赵老师', '导入问题贴近学生经验，课堂节奏初步成型。', '练习分层还可以继续补充。', '草稿阶段建议增加学生展示环节。', 'DRAFT', null, null, null
where not exists (select 1 from observation_record where id = 101);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 102, 4, 3, '王老师', '朗读指导充分，学生参与积极。', '课堂总结与评价标准连接不够明确。', '补充写作评价量规，并在结尾回扣目标。', 'RETURNED', '请补充课堂观察证据和更具体的改进建议。', '2026-04-21 11:20:00', null
where not exists (select 1 from observation_record where id = 102);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 104, 5, 4, '李老师', '阅读任务链清晰，学生能够在问题驱动下完成定位和概括。', '个别学生展示时间略短。', '建议增加同伴互评，让学生说明依据。', 'SUBMITTED', null, '2026-04-22 10:30:00', null
where not exists (select 1 from observation_record where id = 104);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 201, 6, 2, '赵老师', '目标拆解清楚，板书结构帮助学生建立函数图像意识。', '课堂提问可以覆盖更多层次的学生。', '建议设置基础题和挑战题两档追问。', 'APPROVED', null, '2026-04-07 10:20:00', '2026-04-07 16:00:00'
where not exists (select 1 from observation_record where id = 201);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 202, 7, 3, '赵老师', '例题选择贴合生活情境，学生能够主动表达思路。', '小组讨论后的全班反馈稍显仓促。', '保留一组典型错误进行共同辨析。', 'APPROVED', null, '2026-04-12 10:15:00', '2026-04-12 15:40:00'
where not exists (select 1 from observation_record where id = 202);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 203, 8, 4, '赵老师', '复习线索完整，学生能用图像和表达式互相验证。', '课堂末尾方法迁移总结还可以更聚焦。', '建议用一分钟学习单沉淀解题策略。', 'APPROVED', null, '2026-04-18 10:25:00', '2026-04-18 16:10:00'
where not exists (select 1 from observation_record where id = 203);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 204, 9, 2, '王老师', '文本解读层层推进，学生能够抓住关键词。', '板书归纳可以更突出结构化方法。', '建议用思维导图呈现阅读路径。', 'APPROVED', null, '2026-04-08 15:20:00', '2026-04-08 17:00:00'
where not exists (select 1 from observation_record where id = 204);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 205, 10, 3, '王老师', '作文讲评案例具体，学生能看到修改前后的差异。', '学生现场修改的时间偏少。', '建议设置即时修改和展示反馈环节。', 'APPROVED', null, '2026-04-15 15:10:00', '2026-04-15 17:20:00'
where not exists (select 1 from observation_record where id = 205);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 206, 11, 4, '李老师', '听力前任务清楚，学生预测意识较好。', '听后语言输出还可以更充分。', '建议增加复述和观点表达任务。', 'APPROVED', null, '2026-04-16 11:05:00', '2026-04-16 16:30:00'
where not exists (select 1 from observation_record where id = 206);

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_DESIGN', '教学设计', 4.0
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'TEACHING_DESIGN', '教学设计', 4.1
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.0
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'TEACHING_CONTENT', '教学内容', 4.2
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'INTERACTION_FEEDBACK', '互动反馈', 3.8
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'TEACHING_EFFECTIVENESS', '教学效果', 4.0
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_DESIGN', '教学设计', 4.2
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.1
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_CONTENT', '教学内容', 4.3
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'INTERACTION_FEEDBACK', '互动反馈', 4.0
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_EFFECTIVENESS', '教学效果', 4.2
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 201, 'TEACHING_DESIGN', '教学设计', 4.6
where not exists (select 1 from observation_score where record_id = 201 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 201, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.3
where not exists (select 1 from observation_score where record_id = 201 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 201, 'TEACHING_CONTENT', '教学内容', 4.5
where not exists (select 1 from observation_score where record_id = 201 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 201, 'INTERACTION_FEEDBACK', '互动反馈', 4.1
where not exists (select 1 from observation_score where record_id = 201 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 201, 'TEACHING_EFFECTIVENESS', '教学效果', 4.4
where not exists (select 1 from observation_score where record_id = 201 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 202, 'TEACHING_DESIGN', '教学设计', 4.4
where not exists (select 1 from observation_score where record_id = 202 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 202, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.2
where not exists (select 1 from observation_score where record_id = 202 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 202, 'TEACHING_CONTENT', '教学内容', 4.6
where not exists (select 1 from observation_score where record_id = 202 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 202, 'INTERACTION_FEEDBACK', '互动反馈', 4.3
where not exists (select 1 from observation_score where record_id = 202 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 202, 'TEACHING_EFFECTIVENESS', '教学效果', 4.5
where not exists (select 1 from observation_score where record_id = 202 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 203, 'TEACHING_DESIGN', '教学设计', 4.5
where not exists (select 1 from observation_score where record_id = 203 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 203, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.4
where not exists (select 1 from observation_score where record_id = 203 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 203, 'TEACHING_CONTENT', '教学内容', 4.7
where not exists (select 1 from observation_score where record_id = 203 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 203, 'INTERACTION_FEEDBACK', '互动反馈', 4.2
where not exists (select 1 from observation_score where record_id = 203 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 203, 'TEACHING_EFFECTIVENESS', '教学效果', 4.6
where not exists (select 1 from observation_score where record_id = 203 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 204, 'TEACHING_DESIGN', '教学设计', 4.0
where not exists (select 1 from observation_score where record_id = 204 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 204, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.1
where not exists (select 1 from observation_score where record_id = 204 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 204, 'TEACHING_CONTENT', '教学内容', 4.3
where not exists (select 1 from observation_score where record_id = 204 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 204, 'INTERACTION_FEEDBACK', '互动反馈', 4.0
where not exists (select 1 from observation_score where record_id = 204 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 204, 'TEACHING_EFFECTIVENESS', '教学效果', 4.1
where not exists (select 1 from observation_score where record_id = 204 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 205, 'TEACHING_DESIGN', '教学设计', 4.2
where not exists (select 1 from observation_score where record_id = 205 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 205, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.0
where not exists (select 1 from observation_score where record_id = 205 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 205, 'TEACHING_CONTENT', '教学内容', 4.4
where not exists (select 1 from observation_score where record_id = 205 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 205, 'INTERACTION_FEEDBACK', '互动反馈', 4.1
where not exists (select 1 from observation_score where record_id = 205 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 205, 'TEACHING_EFFECTIVENESS', '教学效果', 4.2
where not exists (select 1 from observation_score where record_id = 205 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 206, 'TEACHING_DESIGN', '教学设计', 4.3
where not exists (select 1 from observation_score where record_id = 206 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 206, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.2
where not exists (select 1 from observation_score where record_id = 206 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 206, 'TEACHING_CONTENT', '教学内容', 4.1
where not exists (select 1 from observation_score where record_id = 206 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 206, 'INTERACTION_FEEDBACK', '互动反馈', 4.3
where not exists (select 1 from observation_score where record_id = 206 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 206, 'TEACHING_EFFECTIVENESS', '教学效果', 4.2
where not exists (select 1 from observation_score where record_id = 206 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 1, 'TASK', 1, 'CREATE_TASK', 1, '张组长', '创建听课任务：赵老师数学听课任务-待开始', '2026-04-19 09:00:00'
where not exists (select 1 from audit_log where id = 1);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 2, 'RECORD', 101, 'SAVE_DRAFT', 2, '赵老师', '保存听课记录草稿', '2026-04-21 10:30:00'
where not exists (select 1 from audit_log where id = 2);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 3, 'RECORD', 102, 'REJECT_RECORD', 1, '张组长', '退回听课记录', '2026-04-21 16:00:00'
where not exists (select 1 from audit_log where id = 3);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 4, 'RECORD', 104, 'SUBMIT_RECORD', 4, '李老师', '提交听课记录等待评审', '2026-04-22 10:30:00'
where not exists (select 1 from audit_log where id = 4);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 5, 'RECORD', 201, 'APPROVE_RECORD', 1, '张组长', '通过赵老师听课记录样本 1', '2026-04-07 16:00:00'
where not exists (select 1 from audit_log where id = 5);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 6, 'RECORD', 202, 'APPROVE_RECORD', 1, '张组长', '通过赵老师听课记录样本 2', '2026-04-12 15:40:00'
where not exists (select 1 from audit_log where id = 6);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 7, 'RECORD', 203, 'APPROVE_RECORD', 1, '张组长', '通过赵老师听课记录样本 3', '2026-04-18 16:10:00'
where not exists (select 1 from audit_log where id = 7);

insert into radar_report (id, teacher_name, period_type, period_value, sample_count, radar_json, strength_summary, weakness_summary, conclusion, generated_at)
select 1, '赵老师', 'MONTH', '2026-04', 3, '{"indicators":[{"name":"教学设计","max":5},{"name":"课堂组织","max":5},{"name":"教学内容","max":5},{"name":"互动反馈","max":5},{"name":"教学效果","max":5}],"values":[4.5,4.3,4.6,4.2,4.5]}', '目标拆解清楚；例题选择贴合生活情境；复习线索完整。', '课堂提问覆盖面、小组反馈和方法迁移总结仍可加强。', '已根据 3 条已审批记录生成雷达图。', '2026-04-18 17:00:00'
where not exists (select 1 from radar_report where id = 1);
