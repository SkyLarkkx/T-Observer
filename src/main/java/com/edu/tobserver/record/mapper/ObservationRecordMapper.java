package com.edu.tobserver.record.mapper;

import com.edu.tobserver.record.entity.ObservationRecord;
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
}
