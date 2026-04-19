package com.edu.tobserver.record.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ObservationRecord {

    private Long id;
    private Long taskId;
    private Long observerId;
    private String teacherName;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String status;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
}
