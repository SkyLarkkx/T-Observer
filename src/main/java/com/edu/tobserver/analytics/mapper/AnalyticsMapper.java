package com.edu.tobserver.analytics.mapper;

import com.edu.tobserver.analytics.entity.RadarReport;
import com.edu.tobserver.record.entity.ObservationRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnalyticsMapper {

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
            where status = 'APPROVED'
              and teacher_name = #{teacherName}
              and approved_at >= #{startTime}
              and approved_at < #{endTime}
            order by approved_at desc, id desc
            """)
    List<ObservationRecord> findApprovedRecords(@Param("teacherName") String teacherName,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    @Insert("""
            insert into radar_report
                (teacher_name, period_type, period_value, sample_count, radar_json, strength_summary, weakness_summary, conclusion)
            values
                (#{teacherName}, #{periodType}, #{periodValue}, #{sampleCount}, #{radarJson}, #{strengthSummary}, #{weaknessSummary}, #{conclusion})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RadarReport radarReport);
}
