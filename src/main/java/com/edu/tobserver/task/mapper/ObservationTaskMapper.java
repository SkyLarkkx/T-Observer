package com.edu.tobserver.task.mapper;

import com.edu.tobserver.task.entity.ObservationTask;
import com.edu.tobserver.task.vo.TaskListItemVo;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ObservationTaskMapper {

    @Insert("""
            insert into observation_task
                (title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
            values
                (#{title}, #{leaderId}, #{observerId}, #{teacherName}, #{courseName}, #{lessonTime}, #{deadline}, #{status}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ObservationTask observationTask);

    @Select("""
            <script>
            select
                t.id,
                t.title,
                t.observer_id,
                observer.real_name as observer_name,
                t.teacher_name,
                t.course_name,
                t.lesson_time,
                t.deadline,
                t.status,
                t.remark
            from observation_task t
            left join sys_user observer on observer.id = t.observer_id
            where 1 = 1
            <if test="leaderId != null">
                and t.leader_id = #{leaderId}
            </if>
            <if test="observerId != null">
                and t.observer_id = #{observerId}
            </if>
            <if test="status != null and status != ''">
                and t.status = #{status}
            </if>
            order by t.created_at desc, t.id desc
            </script>
            """)
    List<TaskListItemVo> findList(@Param("leaderId") Long leaderId,
                                  @Param("observerId") Long observerId,
                                  @Param("status") String status);
}
