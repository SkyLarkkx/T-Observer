package com.edu.tobserver.audit.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditLog {

    private Long id;
    private String bizType;
    private Long bizId;
    private String operationType;
    private Long operatorId;
    private String operatorName;
    private String content;
    private LocalDateTime createdAt;
}
