package com.edu.tobserver.record.mapper;

import com.edu.tobserver.record.entity.ObservationScore;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ObservationScoreMapper {

    @Insert("""
            insert into observation_score
                (record_id, dimension_code, dimension_name, score_value)
            values
                (#{recordId}, #{dimensionCode}, #{dimensionName}, #{scoreValue})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ObservationScore observationScore);

    @Delete("""
            delete from observation_score
            where record_id = #{recordId}
            """)
    int deleteByRecordId(@Param("recordId") Long recordId);

    @Select("""
            select
                id,
                record_id,
                dimension_code,
                dimension_name,
                score_value
            from observation_score
            where record_id = #{recordId}
            order by id asc
            """)
    List<ObservationScore> findByRecordId(@Param("recordId") Long recordId);
}
