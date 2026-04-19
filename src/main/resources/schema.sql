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
