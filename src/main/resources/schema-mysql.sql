create table if not exists org_teaching_group (
    id bigint unsigned not null auto_increment comment '自增主键',
    group_code varchar(32) not null comment '教研组编码',
    group_name varchar(64) not null comment '教研组名称',
    subject_code varchar(32) not null comment '学科编码',
    create_time datetime not null default current_timestamp comment '创建时间',
    update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (id),
    unique key uk_group_code (group_code)
) engine=InnoDB default charset=utf8mb4 comment='教研组主数据';

create table if not exists org_teaching_group_member (
    id bigint unsigned not null auto_increment comment '自增主键',
    group_id bigint unsigned not null comment '教研组ID',
    user_id bigint unsigned not null comment '用户ID',
    group_role_code varchar(16) not null comment '组内岗位编码',
    create_time datetime not null default current_timestamp comment '创建时间',
    update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (id),
    unique key uk_group_user (group_id, user_id),
    key idx_member_user_role (user_id, group_role_code)
) engine=InnoDB default charset=utf8mb4 comment='教研组成员关系';

alter table radar_report modify column period_value varchar(64) not null;

set @has_leader_id = (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'radar_report'
      and column_name = 'leader_id'
);
set @leader_id_sql = if(
    @has_leader_id = 0,
    'alter table radar_report add column leader_id bigint not null default 0',
    'select 1'
);
prepare add_leader_id_stmt from @leader_id_sql;
execute add_leader_id_stmt;
deallocate prepare add_leader_id_stmt;

set @has_start_time = (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'radar_report'
      and column_name = 'start_time'
);
set @start_time_sql = if(
    @has_start_time = 0,
    'alter table radar_report add column start_time timestamp null',
    'select 1'
);
prepare add_start_time_stmt from @start_time_sql;
execute add_start_time_stmt;
deallocate prepare add_start_time_stmt;

set @has_end_time = (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'radar_report'
      and column_name = 'end_time'
);
set @end_time_sql = if(
    @has_end_time = 0,
    'alter table radar_report add column end_time timestamp null',
    'select 1'
);
prepare add_end_time_stmt from @end_time_sql;
execute add_end_time_stmt;
deallocate prepare add_end_time_stmt;
