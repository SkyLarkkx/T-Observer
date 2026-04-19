create table if not exists sys_user (
    id bigint primary key auto_increment,
    username varchar(64) not null unique,
    password varchar(128) not null,
    real_name varchar(64) not null,
    role_code varchar(32) not null,
    status varchar(32) not null
);

create table if not exists evaluation_dimension (
    id bigint primary key auto_increment,
    dimension_code varchar(64) not null unique,
    dimension_name varchar(64) not null
);

create table if not exists observation_task (
    id bigint primary key auto_increment,
    title varchar(128) not null,
    leader_id bigint not null,
    observer_id bigint not null,
    teacher_name varchar(64) not null,
    course_name varchar(128) not null,
    lesson_time timestamp not null,
    deadline timestamp not null,
    status varchar(32) not null,
    remark varchar(255),
    created_at timestamp not null default current_timestamp
);

create table if not exists audit_log (
    id bigint primary key auto_increment,
    biz_type varchar(32) not null,
    biz_id bigint not null,
    operation_type varchar(32) not null,
    operator_id bigint not null,
    operator_name varchar(64) not null,
    content varchar(255) not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists observation_record (
    id bigint primary key auto_increment,
    task_id bigint not null,
    observer_id bigint not null,
    teacher_name varchar(64) not null,
    strengths clob not null,
    weaknesses clob not null,
    suggestions clob not null,
    status varchar(32) not null,
    reject_reason varchar(255),
    submitted_at timestamp,
    approved_at timestamp
);

create table if not exists observation_score (
    id bigint primary key auto_increment,
    record_id bigint not null,
    dimension_code varchar(64) not null,
    dimension_name varchar(64) not null,
    score_value decimal(2,1) not null
);
