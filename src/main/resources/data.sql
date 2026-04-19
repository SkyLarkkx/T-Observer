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
