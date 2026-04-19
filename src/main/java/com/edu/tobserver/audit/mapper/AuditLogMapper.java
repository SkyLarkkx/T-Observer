package com.edu.tobserver.audit.mapper;

import com.edu.tobserver.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface AuditLogMapper {

    @Insert("""
            insert into audit_log
                (biz_type, biz_id, operation_type, operator_id, operator_name, content)
            values
                (#{bizType}, #{bizId}, #{operationType}, #{operatorId}, #{operatorName}, #{content})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog auditLog);
}
