package com.edu.tobserver.record.mapper;

import com.edu.tobserver.record.entity.ObservationRecord;
import com.edu.tobserver.review.vo.ReviewListItemVo;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ObservationRecordMapper {

    @Insert("""
            insert into observation_record
                (task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
            values
                (#{taskId}, #{observerId}, #{teacherName}, #{strengths}, #{weaknesses}, #{suggestions}, #{status}, #{rejectReason}, #{submittedAt}, #{approvedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ObservationRecord observationRecord);

    @Update("""
            update observation_record
            set teacher_name = #{teacherName},
                strengths = #{strengths},
                weaknesses = #{weaknesses},
                suggestions = #{suggestions},
                status = #{status},
                reject_reason = #{rejectReason},
                submitted_at = #{submittedAt},
                approved_at = #{approvedAt}
            where id = #{id}
            """)
    int update(ObservationRecord observationRecord);

    @Select("""
            select
                id,
                task_id,
                observer_id,
                teacher_name,
                strengths,
                weaknesses,
                suggestions,
                status,
                reject_reason,
                submitted_at,
                approved_at
            from observation_record
            where task_id = #{taskId}
              and observer_id = #{observerId}
            """)
    ObservationRecord findByTaskIdAndObserverId(@Param("taskId") Long taskId, @Param("observerId") Long observerId);

    @Select("""
            select
                id,
                task_id,
                observer_id,
                teacher_name,
                strengths,
                weaknesses,
                suggestions,
                status,
                reject_reason,
                submitted_at,
                approved_at
            from observation_record
            where id = #{id}
            """)
    ObservationRecord findById(@Param("id") Long id);

    @Select("""
            select
                r.id as record_id,
                r.task_id,
                t.title as task_title,
                observer.real_name as observer_name,
                r.teacher_name,
                t.course_name,
                t.lesson_time,
                t.deadline,
                r.status as record_status,
                r.submitted_at
            from observation_record r
            join observation_task t on t.id = r.task_id
            left join sys_user observer on observer.id = r.observer_id
            where t.leader_id = #{leaderId}
              and r.status in ('SUBMITTED', 'APPROVED', 'RETURNED')
            order by r.submitted_at desc, r.id desc
            """)
    List<ReviewListItemVo> findReviewItemsByLeaderId(@Param("leaderId") Long leaderId);
}
