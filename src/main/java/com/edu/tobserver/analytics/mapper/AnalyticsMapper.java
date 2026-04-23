package com.edu.tobserver.analytics.mapper;

import com.edu.tobserver.analytics.entity.RadarReport;
import com.edu.tobserver.analytics.vo.SavedAnalyticsReportItemVo;
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
            <script>
            select
                r.id,
                r.task_id,
                r.observer_id,
                r.teacher_name,
                r.strengths,
                r.weaknesses,
                r.suggestions,
                r.status,
                r.reject_reason,
                r.submitted_at,
                r.approved_at
            from observation_record r
            join observation_task t on t.id = r.task_id
            where r.status = 'APPROVED'
              and t.leader_id = #{leaderId}
              and r.teacher_name = #{teacherName}
            <if test="startTime != null">
                and r.approved_at &gt;= #{startTime}
            </if>
            <if test="endTime != null">
                and r.approved_at &lt;= #{endTime}
            </if>
            order by r.approved_at desc, r.id desc
            </script>
            """)
    List<ObservationRecord> findApprovedRecords(@Param("leaderId") Long leaderId,
                                                @Param("teacherName") String teacherName,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    @Select("""
            <script>
            select distinct teacher_name
            from observation_task
            where leader_id = #{leaderId}
            <if test="keyword != null and keyword != ''">
                and teacher_name like concat('%', #{keyword}, '%')
            </if>
            order by teacher_name asc
            </script>
            """)
    List<String> findTeacherNames(@Param("leaderId") Long leaderId, @Param("keyword") String keyword);

    @Insert("""
            insert into radar_report
                (leader_id, teacher_name, period_type, period_value, start_time, end_time, sample_count, radar_json, strength_summary, weakness_summary, conclusion)
            values
                (#{leaderId}, #{teacherName}, #{periodType}, #{periodValue}, #{startTime}, #{endTime}, #{sampleCount}, #{radarJson}, #{strengthSummary}, #{weaknessSummary}, #{conclusion})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RadarReport radarReport);

    @Select("""
            select
                id,
                leader_id,
                teacher_name,
                period_type,
                period_value,
                start_time,
                end_time,
                sample_count,
                radar_json,
                strength_summary,
                weakness_summary,
                conclusion,
                generated_at
            from radar_report
            where id = #{id}
              and leader_id = #{leaderId}
            """)
    RadarReport findByIdAndLeaderId(@Param("id") Long id, @Param("leaderId") Long leaderId);

    @Select("""
            select count(*)
            from radar_report
            where leader_id = #{leaderId}
            """)
    long countSavedReports(@Param("leaderId") Long leaderId);

    @Select("""
            select
                id,
                teacher_name,
                sample_count,
                start_time,
                end_time,
                generated_at
            from radar_report
            where leader_id = #{leaderId}
            order by generated_at desc, id desc
            limit #{limit} offset #{offset}
            """)
    List<SavedAnalyticsReportItemVo> findSavedReports(@Param("leaderId") Long leaderId,
                                                      @Param("limit") int limit,
                                                      @Param("offset") int offset);
}
