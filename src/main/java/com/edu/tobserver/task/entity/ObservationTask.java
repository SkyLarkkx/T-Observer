package com.edu.tobserver.task.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ObservationTask {

    private Long id;
    private String title;
    private Long leaderId;
    private Long observerId;
    private String teacherName;
    private String courseName;
    private LocalDateTime lessonTime;
    private LocalDateTime deadline;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
