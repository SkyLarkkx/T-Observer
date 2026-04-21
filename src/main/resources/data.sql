insert into sys_user (username, password, real_name, role_code, status)
select 'leader01', '123456', 'Leader One', 'LEADER', 'ACTIVE'
where not exists (select 1 from sys_user where username = 'leader01');

insert into sys_user (username, password, real_name, role_code, status)
select 'member01', '123456', 'Member One', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where username = 'member01');

insert into sys_user (username, password, real_name, role_code, status)
select 'member02', '123456', 'Member Two', 'MEMBER', 'ACTIVE'
where not exists (select 1 from sys_user where username = 'member02');

insert into sys_user (username, password, real_name, role_code, status)
select 'admin01', '123456', 'Admin One', 'ADMIN', 'ACTIVE'
where not exists (select 1 from sys_user where username = 'admin01');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'TEACHING_DESIGN', 'Teaching Design'
where not exists (select 1 from evaluation_dimension where dimension_code = 'TEACHING_DESIGN');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'CLASSROOM_ORGANIZATION', 'Classroom Organization'
where not exists (select 1 from evaluation_dimension where dimension_code = 'CLASSROOM_ORGANIZATION');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'TEACHING_CONTENT', 'Teaching Content'
where not exists (select 1 from evaluation_dimension where dimension_code = 'TEACHING_CONTENT');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'INTERACTION_FEEDBACK', 'Interaction Feedback'
where not exists (select 1 from evaluation_dimension where dimension_code = 'INTERACTION_FEEDBACK');

insert into evaluation_dimension (dimension_code, dimension_name)
select 'TEACHING_EFFECTIVENESS', 'Teaching Effectiveness'
where not exists (select 1 from evaluation_dimension where dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 1, '高一数学听课 1', 1, 2, '赵老师', '函数概念', '2026-04-20 09:00:00', '2026-04-22 18:00:00', 'COMPLETED', '观察互动'
where not exists (select 1 from observation_task where id = 1);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 2, '高一数学听课 2', 1, 3, '赵老师', '函数概念', '2026-04-21 09:00:00', '2026-04-23 18:00:00', 'COMPLETED', '观察板书'
where not exists (select 1 from observation_task where id = 2);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 3, '高一数学听课 3', 1, 2, '赵老师', '函数概念', '2026-04-22 09:00:00', '2026-04-24 18:00:00', 'COMPLETED', '观察提问'
where not exists (select 1 from observation_task where id = 3);

insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
select 4, 'Task 8 Pending Review Demo', 1, 2, 'Demo Teacher', 'Demo Lesson', '2026-04-23 09:00:00', '2026-04-25 18:00:00', 'COMPLETED', 'Pending review sample'
where not exists (select 1 from observation_task where id = 4);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 101, 1, 2, '赵老师', '课堂节奏清晰，问题导入自然。', '小组讨论时间略短。', '增加学生展示和追问环节。', 'APPROVED', null, '2026-04-20 10:00:00', '2026-04-20 11:00:00'
where not exists (select 1 from observation_record where id = 101);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 102, 2, 3, '赵老师', '板书结构完整，重点突出。', '互动反馈还可以更及时。', '在关键概念处增加即时检测。', 'APPROVED', null, '2026-04-21 10:00:00', '2026-04-21 11:00:00'
where not exists (select 1 from observation_record where id = 102);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 103, 3, 2, '赵老师', '例题层次清楚，学生参与度较高。', '课堂总结可以更聚焦方法迁移。', '结尾增加学生自评和方法归纳。', 'APPROVED', null, '2026-04-22 10:00:00', '2026-04-22 11:00:00'
where not exists (select 1 from observation_record where id = 103);

insert into observation_record (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
select 104, 4, 2, 'Demo Teacher', 'Clear lesson structure and active learner participation.', 'Closure can connect concepts more explicitly.', 'Add a brief student reflection before the final summary.', 'SUBMITTED', null, '2026-04-23 10:00:00', null
where not exists (select 1 from observation_record where id = 104);

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_DESIGN', 'Teaching Design', 4.5
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'CLASSROOM_ORGANIZATION', 'Classroom Organization', 4.2
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_CONTENT', 'Teaching Content', 4.4
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'INTERACTION_FEEDBACK', 'Interaction Feedback', 4.0
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 101, 'TEACHING_EFFECTIVENESS', 'Teaching Effectiveness', 4.3
where not exists (select 1 from observation_score where record_id = 101 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'TEACHING_DESIGN', 'Teaching Design', 4.2
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'CLASSROOM_ORGANIZATION', 'Classroom Organization', 4.5
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'TEACHING_CONTENT', 'Teaching Content', 4.3
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'INTERACTION_FEEDBACK', 'Interaction Feedback', 4.1
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 102, 'TEACHING_EFFECTIVENESS', 'Teaching Effectiveness', 4.4
where not exists (select 1 from observation_score where record_id = 102 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'TEACHING_DESIGN', 'Teaching Design', 4.4
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'CLASSROOM_ORGANIZATION', 'Classroom Organization', 4.3
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'TEACHING_CONTENT', 'Teaching Content', 4.6
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'INTERACTION_FEEDBACK', 'Interaction Feedback', 4.2
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 103, 'TEACHING_EFFECTIVENESS', 'Teaching Effectiveness', 4.5
where not exists (select 1 from observation_score where record_id = 103 and dimension_code = 'TEACHING_EFFECTIVENESS');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_DESIGN', 'Teaching Design', 4.1
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_DESIGN');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'CLASSROOM_ORGANIZATION', 'Classroom Organization', 4.0
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'CLASSROOM_ORGANIZATION');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_CONTENT', 'Teaching Content', 4.2
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_CONTENT');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'INTERACTION_FEEDBACK', 'Interaction Feedback', 3.9
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'INTERACTION_FEEDBACK');

insert into observation_score (record_id, dimension_code, dimension_name, score_value)
select 104, 'TEACHING_EFFECTIVENESS', 'Teaching Effectiveness', 4.0
where not exists (select 1 from observation_score where record_id = 104 and dimension_code = 'TEACHING_EFFECTIVENESS');
