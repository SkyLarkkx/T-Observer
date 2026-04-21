package com.edu.tobserver.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsGenerateRequest {

    @NotBlank(message = "授课教师不能为空")
    private String teacherName;

    @NotBlank(message = "分析开始时间不能为空")
    private String startTime;

    @NotBlank(message = "分析结束时间不能为空")
    private String endTime;
}
