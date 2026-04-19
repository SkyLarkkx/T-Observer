package com.edu.tobserver.audit.service;

import com.edu.tobserver.audit.entity.AuditLog;
import com.edu.tobserver.audit.mapper.AuditLogMapper;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.context.LoginUserContext;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void write(String bizType, Long bizId, String operationType, String content) {
        LoginUser loginUser = LoginUserContext.getRequired();
        AuditLog auditLog = new AuditLog();
        auditLog.setBizType(bizType);
        auditLog.setBizId(bizId);
        auditLog.setOperationType(operationType);
        auditLog.setOperatorId(loginUser.getUserId());
        auditLog.setOperatorName(loginUser.getRealName());
        auditLog.setContent(content);
        auditLogMapper.insert(auditLog);
    }
}
