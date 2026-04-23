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
