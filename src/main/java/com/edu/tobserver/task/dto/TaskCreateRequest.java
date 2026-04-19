package com.edu.tobserver.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    @NotNull(message = "观察成员不能为空")
    private Long observerId;

    @NotBlank(message = "授课教师不能为空")
    private String teacherName;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotNull(message = "听课时间不能为空")
    private LocalDateTime lessonTime;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;

    private String remark;
}
