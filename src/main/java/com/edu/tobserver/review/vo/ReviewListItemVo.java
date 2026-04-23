package com.edu.tobserver.review.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewListItemVo {

    private Long recordId;
    private Long taskId;
    private String taskTitle;
    private String observerName;
    private String teacherName;
    private String courseName;
    private LocalDateTime lessonTime;
    private LocalDateTime deadline;
    private String recordStatus;
    private LocalDateTime submittedAt;
}
