package com.edu.tobserver.task.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TaskListItemVo {

    private Long id;
    private String title;
    private Long observerId;
    private String observerName;
    private String teacherName;
    private String courseName;
    private LocalDateTime lessonTime;
    private LocalDateTime deadline;
    private String status;
    private String remark;
    private String recordStatus;
    private String rejectReason;
}
