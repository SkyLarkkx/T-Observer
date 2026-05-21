-- 种子用户：1 名组长，3 名成员，1 名管理员
insert into sys_user (id, username, password, real_name, role_code, status)
select 1, 'leader01', '123456', '张组长', 'LEADER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 1 or username = 'leader01');

insert into sys_user (id, username, password, real_name, role_code, status)
select 2, 'member01', '123456', '赵老师', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 2 or username = 'member01');

insert into sys_user (id, username, password, real_name, role_code, status)
select 3, 'member02', '123456', '李老师', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 3 or username = 'member02');

insert into sys_user (id, username, password, real_name, role_code, status)
select 4, 'member03', '123456', '王老师', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where id = 4 or username = 'member03');

insert into sys_user (id, username, password, real_name, role_code, status)
select 5, 'admin01', '123456', '系统管理员', 'ADMIN', 'ACTIVE'
where not exists (select 1 from sys_user where id = 5 or username = 'admin01');

-- 固定评价维度
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

-- 教研组与成员关系：演示同一用户可在不同教研组承担不同岗位
insert into org_teaching_group (id, group_code, group_name, subject_code, create_time, update_time)
select 101, 'math-group-01', '数学教研组', 'MATH', '2026-04-20 09:00:00', '2026-04-20 09:00:00'
where not exists (select 1 from org_teaching_group where id = 101 or group_code = 'math-group-01');

insert into org_teaching_group (id, group_code, group_name, subject_code, create_time, update_time)
select 102, 'chinese-group-01', '语文教研组', 'CHINESE', '2026-04-20 09:05:00', '2026-04-20 09:05:00'
where not exists (select 1 from org_teaching_group where id = 102 or group_code = 'chinese-group-01');

insert into org_teaching_group_member (id, group_id, user_id, group_role_code, create_time, update_time)
select 1001, 101, 1, 'LEADER', '2026-04-20 09:10:00', '2026-04-20 09:10:00'
where not exists (select 1 from org_teaching_group_member where id = 1001 or (group_id = 101 and user_id = 1));

insert into org_teaching_group_member (id, group_id, user_id, group_role_code, create_time, update_time)
select 1002, 101, 2, 'MEMBER', '2026-04-20 09:11:00', '2026-04-20 09:11:00'
where not exists (select 1 from org_teaching_group_member where id = 1002 or (group_id = 101 and user_id = 2));

insert into org_teaching_group_member (id, group_id, user_id, group_role_code, create_time, update_time)
select 1003, 101, 3, 'MEMBER', '2026-04-20 09:12:00', '2026-04-20 09:12:00'
where not exists (select 1 from org_teaching_group_member where id = 1003 or (group_id = 101 and user_id = 3));

insert into org_teaching_group_member (id, group_id, user_id, group_role_code, create_time, update_time)
select 1004, 102, 1, 'MEMBER', '2026-04-20 09:13:00', '2026-04-20 09:13:00'
where not exists (select 1 from org_teaching_group_member where id = 1004 or (group_id = 102 and user_id = 1));

insert into org_teaching_group_member (id, group_id, user_id, group_role_code, create_time, update_time)
select 1005, 102, 4, 'LEADER', '2026-04-20 09:14:00', '2026-04-20 09:14:00'
where not exists (select 1 from org_teaching_group_member where id = 1005 or (group_id = 102 and user_id = 4));

-- 听课任务：覆盖待开始、草稿中、待评审、退回后修改和已通过等流程
insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 1, '赵老师数学听课任务-待开始', 1, 2, '赵老师', '函数基础', '2026-04-24 09:00:00', '2026-04-25 18:00:00', 'PENDING', '待成员提交听课记录', '2026-04-23 09:00:00'
where not exists (select 1 from observation_task where id = 1);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 2, '王老师语文听课任务-待开始', 1, 3, '王老师', '阅读策略', '2026-04-24 14:00:00', '2026-04-26 18:00:00', 'PENDING', '另一条待开始任务，用于分页和筛选', '2026-04-23 09:10:00'
where not exists (select 1 from observation_task where id = 2);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 3, '李老师英语听课任务-草稿中', 1, 4, '李老师', '听力训练', '2026-04-23 10:00:00', '2026-04-25 18:00:00', 'IN_PROGRESS', '成员已保存草稿，但尚未提交', '2026-04-22 16:00:00'
where not exists (select 1 from observation_task where id = 3);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 4, '赵老师数学听课任务-已退回', 1, 2, '赵老师', '几何证明', '2026-04-22 09:00:00', '2026-04-24 18:00:00', 'IN_PROGRESS', '已退回一次，等待修改后重新提交', '2026-04-21 15:30:00'
where not exists (select 1 from observation_task where id = 4);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 5, '王老师语文听课任务-待评审', 1, 3, '王老师', '作文讲评', '2026-04-22 15:00:00', '2026-04-24 18:00:00', 'COMPLETED', '已提交，等待组长评审', '2026-04-21 16:00:00'
where not exists (select 1 from observation_task where id = 5);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 6, '赵老师数学听课任务-已通过 1', 1, 2, '赵老师', '函数图像', '2026-04-07 09:00:00', '2026-04-09 18:00:00', 'COMPLETED', '赵老师分析样本 A', '2026-04-06 10:00:00'
where not exists (select 1 from observation_task where id = 6);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 7, '赵老师数学听课任务-已通过 2', 1, 3, '赵老师', '二次函数', '2026-04-12 09:00:00', '2026-04-14 18:00:00', 'COMPLETED', '赵老师分析样本 B', '2026-04-11 10:00:00'
where not exists (select 1 from observation_task where id = 7);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 8, '赵老师数学听课任务-已通过 3', 1, 4, '赵老师', '综合复习', '2026-04-18 09:00:00', '2026-04-20 18:00:00', 'COMPLETED', '赵老师分析样本 C', '2026-04-17 11:00:00'
where not exists (select 1 from observation_task where id = 8);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 9, '王老师语文听课任务-已通过 1', 1, 2, '王老师', '现代文阅读', '2026-04-08 14:00:00', '2026-04-10 18:00:00', 'COMPLETED', '其他教师的已通过样本', '2026-04-07 13:00:00'
where not exists (select 1 from observation_task where id = 9);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 10, '李老师英语听课任务-已通过 1', 1, 3, '李老师', '英语听力', '2026-04-16 10:00:00', '2026-04-18 18:00:00', 'COMPLETED', '低样本分析样本', '2026-04-15 08:00:00'
where not exists (select 1 from observation_task where id = 10);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark, created_at)
select 11, '赵老师数学听课任务-待评审 2', 1, 4, '赵老师', '方程求解', '2026-04-23 14:00:00', '2026-04-25 18:00:00', 'COMPLETED', '另一条已提交、等待评审的记录', '2026-04-22 09:30:00'
where not exists (select 1 from observation_task where id = 11);

-- 听课记录
insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 101, 3, 4, '李老师', '导入说明清晰，学生能够迅速进入学习状态。', '草稿中的听力目标表述还不够明确。', '建议在练习前增加一句话，明确本节听力训练目标。', 'DRAFT', null, null, null
where not exists (select 1 from observation_record where id = 101);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 102, 4, 2, '赵老师', '板书结构清晰，关键步骤呈现明确。', '对学生易错点纠正的课堂证据还不够充分。', '建议补充一个教师纠正学生误区的具体课堂片段。', 'RETURNED', '请补充更充分的课堂观察证据，并明确改进建议。', '2026-04-22 10:20:00', null
where not exists (select 1 from observation_record where id = 102);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 103, 5, 3, '王老师', '作文评价量规讲解具体，配有清晰示例。', '学生现场修改时间略显不足。', '建议增加五分钟同伴互评与即时修改时间。', 'SUBMITTED', null, '2026-04-22 16:30:00', null
where not exists (select 1 from observation_record where id = 103);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 104, 6, 2, '赵老师', '教学目标拆解清楚，例题与学习内容贴合。', '课堂提问还可以覆盖更多中间层学生。', '建议针对不同层次学生预设一条追问问题。', 'APPROVED', null, '2026-04-07 10:10:00', '2026-04-07 16:00:00'
where not exists (select 1 from observation_record where id = 104);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 105, 7, 3, '赵老师', '学生能借助图像清晰说明函数变化过程。', '两人讨论后的全班反馈环节稍显简短。', '建议保留一个典型错误案例，供全班辨析修正。', 'APPROVED', null, '2026-04-12 10:00:00', '2026-04-12 15:30:00'
where not exists (select 1 from observation_record where id = 105);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 106, 8, 4, '赵老师', '复习线索完整，图像与公式之间的联系处理得较好。', '课堂结尾的方法总结还可以再凝练一些。', '建议用一分钟学习卡沉淀解题策略。', 'APPROVED', null, '2026-04-18 10:15:00', '2026-04-18 16:10:00'
where not exists (select 1 from observation_record where id = 106);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 107, 9, 2, '王老师', '文本解读由细节推进到主题，层次清晰。', '板书归纳还可以更突出阅读路径。', '建议用小型思维导图总结阅读结构。', 'APPROVED', null, '2026-04-08 15:05:00', '2026-04-08 17:00:00'
where not exists (select 1 from observation_record where id = 107);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 108, 10, 3, '李老师', '听前任务设置清楚，学生能主动预测内容。', '听后口语输出环节仍然偏少。', '建议在第二轮听后增加一个简短复述任务。', 'APPROVED', null, '2026-04-16 11:00:00', '2026-04-16 16:20:00'
where not exists (select 1 from observation_record where id = 108);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 109, 11, 4, '赵老师', '教师对方程求解步骤讲解清楚，并能及时检查理解情况。', '结尾巩固还不够充分，收束力度略弱。', '建议补充两种解法的简短对比。', 'SUBMITTED', null, '2026-04-23 15:20:00', null
where not exists (select 1 from observation_record where id = 109);

-- 草稿与退回记录评分
insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_DESIGN', '教学设计', 4.0
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.1
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_CONTENT', '教学内容', 4.0
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'INTERACTION_FEEDBACK', '互动反馈', 3.9
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_EFFECTIVENESS', '教学效果', 4.0
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_EFFECTIVENESS');

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

-- 待评审记录评分
insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'TEACHING_DESIGN', '教学设计', 4.2
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.1
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'TEACHING_CONTENT', '教学内容', 4.3
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'INTERACTION_FEEDBACK', '互动反馈', 4.0
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'TEACHING_EFFECTIVENESS', '教学效果', 4.2
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 109, 'TEACHING_DESIGN', '教学设计', 4.3
where not exists (select 1 from observation_score where record_id = 109 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 109, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.2
where not exists (select 1 from observation_score where record_id = 109 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 109, 'TEACHING_CONTENT', '教学内容', 4.4
where not exists (select 1 from observation_score where record_id = 109 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 109, 'INTERACTION_FEEDBACK', '互动反馈', 4.1
where not exists (select 1 from observation_score where record_id = 109 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 109, 'TEACHING_EFFECTIVENESS', '教学效果', 4.2
where not exists (select 1 from observation_score where record_id = 109 and dimension_code = 'TEACHING_EFFECTIVENESS');

-- 已通过记录评分
insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_DESIGN', '教学设计', 4.6
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.3
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_CONTENT', '教学内容', 4.5
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'INTERACTION_FEEDBACK', '互动反馈', 4.1
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_EFFECTIVENESS', '教学效果', 4.4
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 105, 'TEACHING_DESIGN', '教学设计', 4.4
where not exists (select 1 from observation_score where record_id = 105 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 105, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.2
where not exists (select 1 from observation_score where record_id = 105 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 105, 'TEACHING_CONTENT', '教学内容', 4.6
where not exists (select 1 from observation_score where record_id = 105 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 105, 'INTERACTION_FEEDBACK', '互动反馈', 4.3
where not exists (select 1 from observation_score where record_id = 105 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 105, 'TEACHING_EFFECTIVENESS', '教学效果', 4.5
where not exists (select 1 from observation_score where record_id = 105 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 106, 'TEACHING_DESIGN', '教学设计', 4.5
where not exists (select 1 from observation_score where record_id = 106 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 106, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.4
where not exists (select 1 from observation_score where record_id = 106 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 106, 'TEACHING_CONTENT', '教学内容', 4.7
where not exists (select 1 from observation_score where record_id = 106 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 106, 'INTERACTION_FEEDBACK', '互动反馈', 4.2
where not exists (select 1 from observation_score where record_id = 106 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 106, 'TEACHING_EFFECTIVENESS', '教学效果', 4.6
where not exists (select 1 from observation_score where record_id = 106 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 107, 'TEACHING_DESIGN', '教学设计', 4.0
where not exists (select 1 from observation_score where record_id = 107 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 107, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.1
where not exists (select 1 from observation_score where record_id = 107 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 107, 'TEACHING_CONTENT', '教学内容', 4.3
where not exists (select 1 from observation_score where record_id = 107 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 107, 'INTERACTION_FEEDBACK', '互动反馈', 4.0
where not exists (select 1 from observation_score where record_id = 107 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 107, 'TEACHING_EFFECTIVENESS', '教学效果', 4.1
where not exists (select 1 from observation_score where record_id = 107 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 108, 'TEACHING_DESIGN', '教学设计', 4.3
where not exists (select 1 from observation_score where record_id = 108 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 108, 'CLASSROOM_ORGANIZATION', '课堂组织', 4.2
where not exists (select 1 from observation_score where record_id = 108 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 108, 'TEACHING_CONTENT', '教学内容', 4.1
where not exists (select 1 from observation_score where record_id = 108 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 108, 'INTERACTION_FEEDBACK', '互动反馈', 4.3
where not exists (select 1 from observation_score where record_id = 108 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 108, 'TEACHING_EFFECTIVENESS', '教学效果', 4.2
where not exists (select 1 from observation_score where record_id = 108 and dimension_code = 'TEACHING_EFFECTIVENESS');

-- 审计日志示例
insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 1, 'TASK', 1, 'CREATE_TASK', 1, '张组长', '创建听课任务：赵老师数学听课任务-待开始', '2026-04-23 09:00:00'
where not exists (select 1 from audit_log where id = 1);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 2, 'RECORD', 101, 'SAVE_DRAFT', 4, '王老师', '保存李老师听课记录草稿', '2026-04-23 11:00:00'
where not exists (select 1 from audit_log where id = 2);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 3, 'RECORD', 102, 'REJECT_RECORD', 1, '张组长', '退回听课记录并要求补充课堂证据', '2026-04-22 14:00:00'
where not exists (select 1 from audit_log where id = 3);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 4, 'RECORD', 103, 'SUBMIT_RECORD', 3, '李老师', '提交听课记录，等待组长评审', '2026-04-22 16:30:00'
where not exists (select 1 from audit_log where id = 4);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 5, 'RECORD', 104, 'APPROVE_RECORD', 1, '张组长', '通过赵老师听课记录样本 A', '2026-04-07 16:00:00'
where not exists (select 1 from audit_log where id = 5);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 6, 'RECORD', 105, 'APPROVE_RECORD', 1, '张组长', '通过赵老师听课记录样本 B', '2026-04-12 15:30:00'
where not exists (select 1 from audit_log where id = 6);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 7, 'RECORD', 106, 'APPROVE_RECORD', 1, '张组长', '通过赵老师听课记录样本 C', '2026-04-18 16:10:00'
where not exists (select 1 from audit_log where id = 7);

insert into audit_log (id, biz_type, biz_id, operation_type, operator_id, operator_name, content, created_at)
select 8, 'ANALYTICS', 1, 'SAVE_REPORT', 1, '张组长', '保存赵老师分析报告', '2026-04-18 17:00:00'
where not exists (select 1 from audit_log where id = 8);

-- 已保存的雷达分析报告
insert into radar_report (id, leader_id, teacher_name, period_type, period_value, start_time, end_time, sample_count, radar_json, strength_summary, weakness_summary, conclusion, generated_at)
select
    1,
    1,
    '赵老师',
    'RANGE',
    '2026-04-01 00:00 至 2026-04-30 23:59',
    '2026-04-01 00:00:00',
    '2026-04-30 23:59:00',
    3,
    '{"indicators":[{"name":"教学设计","max":5},{"name":"课堂组织","max":5},{"name":"教学内容","max":5},{"name":"互动反馈","max":5},{"name":"教学效果","max":5}],"values":[4.5,4.3,4.6,4.2,4.5]}',
    '教学目标拆解清楚，例题贴近内容，复习线索连贯完整。',
    '课堂提问覆盖面和结尾策略总结仍可进一步加强。',
    '已根据 3 条已通过记录生成分析报告。',
    '2026-04-18 17:00:00'
where not exists (select 1 from radar_report where id = 1);

insert into radar_report (id, leader_id, teacher_name, period_type, period_value, start_time, end_time, sample_count, radar_json, strength_summary, weakness_summary, conclusion, generated_at)
select
    2,
    1,
    '王老师',
    'RANGE',
    '2026-04-01 00:00 至 2026-04-30 23:59',
    '2026-04-01 00:00:00',
    '2026-04-30 23:59:00',
    1,
    '{"indicators":[{"name":"教学设计","max":5},{"name":"课堂组织","max":5},{"name":"教学内容","max":5},{"name":"互动反馈","max":5},{"name":"教学效果","max":5}],"values":[4.0,4.1,4.3,4.0,4.1]}',
    '阅读路径清晰，教师能够稳定引导学生由细节走向主题。',
    '板书归纳和结构化呈现仍是主要改进点。',
    '已根据 1 条已通过记录生成分析报告。',
    '2026-04-08 17:10:00'
where not exists (select 1 from radar_report where id = 2);
